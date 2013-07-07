<div id="app-navbar-container" class="container">
    <div id="app-navbar" class="navbar">
        <div id="app-navbar-inner" class="navbar-inner">
            <div id="inner-navbar-container" class="container">

				<h4 class="app-navbar-brand visible-desktop hidden-tablet hidden-phone">USGS Coastal Change Hazards Portal</h4>
                <h4 class="app-navbar-brand hidden-desktop visible-tablet hidden-phone">Coastal Change Hazards Portal</h4>
				<h4 class="app-navbar-brand hidden-desktop hidden-tablet visible-phone">USGS CCH</h4>

				<div id="app-navbar-pin-control" class="btn-group">
					<button id='app-navbar-pin-control-button' class="btn btn-mini disabled"><i id='app-navbar-pin-control-icon' class="icon-pushpin muted"></i>&nbsp;<span id='app-navbar-pin-control-pincount'>0</span></button>
					<button id='app-navbar-pin-control-dropdown-button' class="btn btn-mini dropdown-toggle disabled" data-toggle="dropdown"><span id='app-navbar-pin-control-caret' class="icon-caret-down"></span></button>
					<ul class="dropdown-menu">
						<li id='app-navbar-pin-control-clear-li'><a id='app-navbar-pin-control-clear' href="#">Clear View</a></li>
						<li id='app-navbar-pin-control-share-li'><a tabindex="-1" data-toggle="modal" role="button" href="#shareModal">Share View</a></li>
					</ul>
				</div>
				<div id="shareModal" class="modal fade"  role="dialog" aria-labelledby="modal-label" aria-hidden="true">
					<div class="modal-header">
						<h4 id="modal-label">Share Your Coastal Change Hazards Portal View With Others</h4>
					</div>
					<div class="modal-body">
						<div class="row-fluid">
							<div class="well well-small">
								<div id="modal-share-summary-url-inputbox-div">
									<input id="modal-share-summary-url-inputbox" type='text' autofocus readonly size="20" placeholder="Loading..." />
								</div>
								<a id="modal-share-summary-url-button" class="btn" target="_blank" role="button">View In Portal</a>
								
							</div>
							<span class="pull-right" id='multi-card-twitter-button'></span>
						</div>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn"  data-dismiss="modal" aria-hidden="true">Close</a>
					</div>
				</div>

				<span id="app-navbar-item-search-container" class="pull-right">
					<i class="icon-search"></i>
				</span>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
	$('#site-title').remove();
</script>