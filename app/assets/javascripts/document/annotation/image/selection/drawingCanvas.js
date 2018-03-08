define(['common/hasEvents'], function(HasEvents) {

  var ZOOM_DURATION = 250,
      MIN_DRAG_TIME = 300; // Everything below that threshold = click, otherwise drag

  var DrawingCanvas = function(containerEl, olMap) {

    var self = this,

        canvas =
          jQuery('<canvas class="drawing-canvas"></canvas>').hide().appendTo(containerEl),

        isMouseDown = false,

        lastClickTime,

        // DUMMY - for testing only
        eventFwdEnabled = true,

        olView = olMap.getView(),

        centerXY = [ jQuery(containerEl).width() / 2, jQuery(containerEl).height() / 2 ],

        init = function() {
          canvas.attr('width', canvas.width());
          canvas.attr('height', canvas.height());

          // Keep track of mouse button state
          canvas.mousedown(function() { isMouseDown = true; });
          canvas.mouseup(function() { isMouseDown = false; });

          // Forward move and scroll events
          canvas.mousemove(onMouseMove);
          canvas.mousedown(onMouseDown);
          canvas.mouseup(onMouseUp);
          canvas.bind('wheel', onMouseWheel);
        },

        /**
         * Mouse move events are fired via self.fireEvent, and forwarded to
         * OpenLayers if required.
         */
        onMouseMove = function(e) {
          var forwardMouseMove = function() {
                var dx = e.originalEvent.movementX,
                    dy = e.originalEvent.movementY;

                    destXY = [ centerXY[0] - dx, centerXY[1] - dy ];
                    center = olMap.getCoordinateFromPixel(destXY);

                olView.setCenter(center);
              };

          self.fireEvent('mousemove', e);
          if (isMouseDown && eventFwdEnabled) forwardMouseMove();
        },

        onMouseDown = function(e) {
          lastClickTime = new Date().getTime();
        },

        onMouseUp = function(e) {
          var now = new Date().getTime();
          if ((now - lastClickTime) < MIN_DRAG_TIME)
            self.fireEvent('click', e);
        },

        /** Mouse wheel events are always forwarded to OpenLayers **/
        onMouseWheel = function(e) {
          var delta = e.originalEvent.deltaY,
              dir = delta / Math.abs(delta);

          olView.animate({ zoom: olView.getZoom() - dir, duration: ZOOM_DURATION });
        },

        setEventForwardingEnabled = function(enabled) {
          eventFwdEnabled = enabled;
        },

        show = function() {
          canvas.show();
        },

        hide = function() {
          canvas.hide();
        };

    init();

    this.hide = hide;
    this.show = show;
    this.setEventForwardingEnabled = setEventForwardingEnabled;

    // Properties
    this.ctx = canvas.get(0).getContext('2d');
    this.width = canvas.get(0).width;
    this.height = canvas.get(0).height;

    HasEvents.apply(this);
  };
  DrawingCanvas.prototype = Object.create(HasEvents.prototype);

  return DrawingCanvas;

});
