package gov.usgs.cida.coastalhazards.rest.ui;

import gov.usgs.cida.coastalhazards.jpa.AliasManager;
import gov.usgs.cida.coastalhazards.jpa.ItemManager;
import gov.usgs.cida.coastalhazards.model.Alias;
import gov.usgs.cida.coastalhazards.model.Item;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.mvc.Viewable;

/**
 *
 * @author isuftin
 */
@Path("/print")
public class PrintRouter {

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/item/{id}")
	public Response useInfoPrintViewJsp(@PathParam("id") String id) {
		Map<String, Object> map = new HashMap<>();
		try (ItemManager mgr = new ItemManager()) {
			Item item = mgr.load(id);
			if (item == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			map.put("item", item);
			return Response.ok(new Viewable("/WEB-INF/jsp/ui/back/index-print.jsp", map)).build();
		}

	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/alias/{aliasId}")
	public Response useAliasInfoPrintViewJsp(@PathParam("aliasId") String aliasId) {
		Map<String, Object> map = new HashMap<>();
		try (ItemManager mgr = new ItemManager(); AliasManager amgr = new AliasManager();) {
			Alias alias = amgr.load(aliasId);
			if(alias != null){
				Item item = mgr.load(alias.getItemId());
				if (item == null) {
					return Response.status(Response.Status.NOT_FOUND).build();
				}
				map.put("item", item);
				map.put("alias", alias);
				return Response.ok(new Viewable("/WEB-INF/jsp/ui/back/index-print.jsp", map)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build(); 
			}
		}
	}

}
