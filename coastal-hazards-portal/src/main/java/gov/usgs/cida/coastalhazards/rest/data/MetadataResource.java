package gov.usgs.cida.coastalhazards.rest.data;

import com.google.gson.JsonSyntaxException;
import gov.usgs.cida.coastalhazards.gson.GsonUtil;
import gov.usgs.cida.coastalhazards.jpa.ItemManager;
import gov.usgs.cida.coastalhazards.model.summary.Publication;
import gov.usgs.cida.coastalhazards.model.Bbox;
import gov.usgs.cida.coastalhazards.model.Item;
import gov.usgs.cida.coastalhazards.model.Service;
import gov.usgs.cida.coastalhazards.model.summary.Summary;
import gov.usgs.cida.coastalhazards.rest.data.util.MetadataUtil;
import gov.usgs.cida.coastalhazards.rest.security.CoastalHazardsTokenBasedSecurityFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.referencing.CRS;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author isuftin
 */
@Path(DataURI.METADATA_PATH)
@PermitAll //says that all methods, unless otherwise secured, will be allowed by default
public class MetadataResource {

	private static final Logger log = LoggerFactory.getLogger(MetadataResource.class);
	private static File UPLOAD_DIR;
	private static String latestMetadata;

	public MetadataResource() {
		super();
		UPLOAD_DIR = new File(FileUtils.getTempDirectoryPath() + "/metadata-upload");
	}
	
	@GET
	@Path("/latest")
	public Response getLatestMetadata() {
		return Response.ok(latestMetadata).build();
	}
	
	public void setLatestMetadata(String stormMetadata) {
		latestMetadata = stormMetadata;
	}
		
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({CoastalHazardsTokenBasedSecurityFilter.CCH_ADMIN_ROLE})
	public Response getMetadata(@Context HttpServletRequest req, @FormDataParam("file") String postBody) {
		setLatestMetadata(postBody);
		Response response = Response.ok(postBody).build();
		Document doc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		List<String> title = new ArrayList<>();
		List<String> srcUsed = new ArrayList<>();
		List<String> keywords = new ArrayList<>();
		List<Publication> data = new ArrayList<>();
		List<Publication> publication = new ArrayList<>();
		List<Publication> resource = new ArrayList<>();
		Bbox box = new Bbox();
		
		try {
			doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(postBody)));
			doc.getDocumentElement().normalize();
			
			title.addAll(MetadataUtil.extractStringsFromCswDoc(doc, "/metadata/idinfo/citation/citeinfo/title"));
			srcUsed.addAll(MetadataUtil.extractStringsFromCswDoc(doc, "/metadata/dataqual/lineage/procstep/srcused"));
			
			box = MetadataUtil.getBoundingBoxFromFgdcMetadata(postBody);
			keywords.addAll(MetadataUtil.extractStringsFromCswDoc(doc, "//*/placekey"));
			keywords.addAll(MetadataUtil.extractStringsFromCswDoc(doc, "//*/themekey"));
			
			data.addAll(MetadataUtil.getResourcesFromXml(doc, "citation"));
			publication.addAll(MetadataUtil.getResourcesFromXml(doc, "lworkcit"));
			resource.addAll(MetadataUtil.getResourcesFromXml(doc, "crossref"));
			resource.addAll(MetadataUtil.getResourcesFromXml(doc, "srccite"));		
			
			Map<String, Object> grouped = new HashMap<>();
			grouped.put("title", title);
			grouped.put("srcUsed", srcUsed);
			grouped.put("Box", box);
			grouped.put("Keywords", keywords);
			grouped.put("Data", data);
			grouped.put("Publications", publication);
			grouped.put("Resources", resource);

			// Only some, generally raster, metadata xml files will include EPSG data
			try {
				String epsgCode = CRS.lookupIdentifier(MetadataUtil.getCrsFromFgdcMetadata(postBody), true);
				grouped.put("EPSGCode", epsgCode);
			} catch (Exception e) {
				log.info("Unable to extract an EPSG code from metadata XML; This is not an error. Returning null. Reason: " + e.getMessage());
			}
			
			response = Response.ok(GsonUtil.getDefault().toJson(grouped, Map.class)).build();
			
		} catch (Exception e) {
			log.error("Failed to parse metadata xml document. Error: " + e.getMessage() + ". Stack Trace: " + e.getStackTrace());
		}

		return response;
	}

	@GET
	@Path("/{fid}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getFileById(@PathParam("fid") String fid) throws IOException {
		File readFile = new File(UPLOAD_DIR, fid);
		Map<String, String> responseContent = new HashMap<>();
		if (!readFile.exists()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		else if (!readFile.canRead()) {
			responseContent.put("message", "Metadata Not Accessible");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseContent).build();
		}
		else {
			return Response.ok(IOUtils.toString(new FileInputStream(readFile)), MediaType.APPLICATION_XML_TYPE).build();
		}
	}

	@GET
	@Path("/summarize/itemid/{itemid}/attribute/{attr}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMetadataSummaryByAttribtueUsingItemID(
		@PathParam("itemid") String itemId,
		@PathParam("attr") String attr
	) throws URISyntaxException {
		Response response;
		try (ItemManager itemManager = new ItemManager()) {
			Item item = itemManager.load(itemId);
			String jsonSummary = MetadataUtil.getSummaryFromWPS(getMetadataUrl(item), attr);
			Summary summary = GsonUtil.getDefault().fromJson(jsonSummary, Summary.class);
			response = Response.ok(GsonUtil.getDefault().toJson(summary, Summary.class), MediaType.APPLICATION_JSON_TYPE).build();
		}
		catch (IOException | ParserConfigurationException | SAXException | JsonSyntaxException ex) {
			Map<String, String> err = new HashMap<>();
			err.put("message", ex.getMessage());
			response = Response.serverError().entity(GsonUtil.getDefault().toJson(err, HashMap.class)).build();
		}
		return response;
	}

	@GET
	@Path("/summarize/fid/{fid}/attribute/{attr}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMetadataSummaryByAttribtueUsingFD(@PathParam("fid") String fid,
			@PathParam("attr") String attr) throws URISyntaxException {
		Response response;
		try {
			String jsonSummary = MetadataUtil.getSummaryFromWPS(fid, attr);
			Summary summary = GsonUtil.getDefault().fromJson(jsonSummary, Summary.class);
			response = Response.ok(GsonUtil.getDefault().toJson(summary, Summary.class), MediaType.APPLICATION_JSON_TYPE).build();
		}
		catch (IOException | ParserConfigurationException | SAXException | JsonSyntaxException ex) {
			Map<String, String> err = new HashMap<>();
			err.put("message", ex.getMessage());
			response = Response.serverError().entity(GsonUtil.getDefault().toJson(err, HashMap.class)).build();
		}
		return response;
	}

	private static String getMetadataUrl(Item item) {
		String url = "";
		if (item != null) {
			List<Service> services = item.getServices();
			for (Service service : services) {
				if (service.getType() == Service.ServiceType.csw) {
					url = service.getEndpoint();
				}
			}
		}
		return url;
	}

}
