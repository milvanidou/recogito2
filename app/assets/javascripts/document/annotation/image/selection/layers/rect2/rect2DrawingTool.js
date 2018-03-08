define([
  'common/config',
  'document/annotation/image/selection/layers/geom2D',
  'document/annotation/image/selection/layers/layer',
  'document/annotation/image/selection/layers/style'
], function(Config, Geom2D, Layer, Style) {

      /** Constants **/
  var MIN_DRAG_TIME = 300;   // Minimum duration of an annotation drag (milliseconds)

  var Rect2DrawingTool = function(canvas, olMap) {

    var self = this,

        /// ctx = canvas.getContext('2d'),

        /** Mouse state **/
        lastClickTime,
        isMouseDown = false,

        attachMouseHandlers = function() {
          /* Handlers on the drawing canvas
          var c = jQuery(canvas);
          c.mousedown(onMouseDown);
          c.mousemove(onMouseMove);
          c.mouseup(onMouseUp);
          */
        },

        updateSize = function() {
          /*
          var c = jQuery(canvas);
          canvas.width = c.width();
          canvas.height = c.height();
          ctx.strokeStyle = Style.COLOR_RED;
          ctx.lineWidth = Style.BOX_BASELINE_WIDTH;
          */
        },

        clearCanvas = function() {
          // ctx.clearRect(0, 0, canvas.width, canvas.height);
        },

        startPainting = function(e) {
          canvas.show();
          /*
          painting = true;
          anchorX = (e.offsetX) ? e.offsetX : e.originalEvent.layerX;
          anchorY = (e.offsetY) ? e.offsetY : e.originalEvent.layerY;

          // Paints a redundant line of 0px length - but saves a few duplicate lines of code
          paintBaseline(e);
          */
        },

        clearSelection = function() {
          clearCanvas();
        },

        setEnabled = function(enabled) {
          if (enabled)
            canvas.show();
          else
            canvas.hide();
        },

        onMouseDown = function(jqEvt) {

        },

        onMouseMove = function(jqEvt) {

        },

        onMouseUp = function(e) {

        };

    updateSize();
    attachMouseHandlers();

    // Reset canvas on window resize
    jQuery(window).on('resize', updateSize);

    this.clearSelection = clearSelection;
    this.setEnabled = setEnabled;
    this.updateSize = updateSize;

    Layer.apply(this, [ olMap ]);
  };
  Rect2DrawingTool.prototype = Object.create(Layer.prototype);

  return Rect2DrawingTool;

});
