define([], function() {

  var ZOOM_DURATION = 100;

  var DrawingCanvas = function(containerEl, olMap) {

    var canvas =
          jQuery('<canvas class="drawing-canvas"></canvas>').hide().appendTo(containerEl),

        isMouseDown = false,

        // DUMMY - for testing only
        eventFwdEnabled = true,

        olView = olMap.getView(),

        centerXY = [ jQuery(containerEl).width() / 2, jQuery(containerEl).height() / 2 ],

        attachListeners = function() {
          // Keep track of mouse button state
          canvas.mousedown(function() { isMouseDown = true; });
          canvas.mouseup(function() { isMouseDown = false; });

          // Forward move and scroll events
          canvas.mousemove(forwardMouseMove);
          canvas.bind('wheel', forwardMouseWheel);
        },

        forwardMouseMove = function(e) {
          var dx = e.originalEvent.movementX,
              dy = e.originalEvent.movementY;

              destXY = [ centerXY[0] - dx, centerXY[1] - dy ];
              center = olMap.getCoordinateFromPixel(destXY);

          if (isMouseDown && eventFwdEnabled) olView.setCenter(center);
        },

        forwardMouseWheel = function(e) {
          var delta = e.originalEvent.deltaY,
              dir = delta / Math.abs(delta);

          olView.animate({ zoom: olView.getZoom() - dir, duration: ZOOM_DURATION });
        },

        setEventForwardingEnabled = function(enabled) {

        },

        show = function() {
          canvas.show();
        },

        hide = function() {
          canvas.hide();
        };

    attachListeners();

    this.hide = hide;
    this.show = show;
  };

  return DrawingCanvas;

});
