define([
  'common/config',
  'document/annotation/image/selection/layers/geom2D',
  'document/annotation/image/selection/layers/layer',
  'document/annotation/image/selection/layers/style'
], function(Config, Geom2D, Layer, Style) {

      /** Constants **/
  var TWO_PI = 2 * Math.PI;

  var Rect2DrawingTool = function(canvas, olMap) {

    var self = this,

        mouseX, mouseY,

        drawing = false,

        // Option[{ top, right, bottom, left }]
        currentRect = false,

        onMouseMove = function(e) {
          mouseX = e.offsetX;
          mouseY = e.offsetY;

          if (drawing) {
            currentRect.right = e.offsetX;
            currentRect.bottom = e.offsetY;
          }
        },

        onMouseClick = function(e) {
          if (drawing) {
            drawing = false; // Stop drawing
          } else if (currentRect) {
            currentRect = false; // Reset
          } else { // Start drawing
            currentRect = { top: e.offsetY, left: e.offsetX };
            drawing = true;
          }
        },

        drawDot = function(ctx, x, y) {
          ctx.beginPath();
          ctx.lineWidth = 4;
          ctx.shadowBlur = 6;
          ctx.shadowColor = 'rgba(0, 0, 0, 0.6)';
          ctx.strokeStyle = 'rgba(128, 82, 32, 0.65)';
          ctx.arc(x, y, 7, 0, TWO_PI);
          ctx.stroke();

          ctx.beginPath();
          ctx.shadowBlur = 0;
          ctx.lineWidth = 2.5;
          ctx.strokeStyle = '#fff';
          ctx.fillStyle = 'orange';
          ctx.arc(x, y, 7, 0, TWO_PI);
          ctx.fill();
          ctx.stroke();
        },

        drawCurrentRect = function(ctx) {
          var w = currentRect.right - currentRect.left,
              h = currentRect.bottom - currentRect.top;

          ctx.beginPath();
          ctx.shadowBlur = 0;
          ctx.lineWidth = 2.5;
          ctx.strokeStyle = 'orange';
          ctx.rect(currentRect.left, currentRect.top, w, h);
          ctx.stroke();

          drawDot(ctx, currentRect.left, currentRect.top);
          drawDot(ctx, currentRect.right, currentRect.bottom);
        },

        render = function() {
          canvas.ctx.clearRect(0, 0, canvas.width, canvas.height);

          if (currentRect) drawCurrentRect(canvas.ctx);
          if (!drawing) drawDot(canvas.ctx, mouseX, mouseY);

          requestAnimationFrame(render);
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

        startPainting = function() {
          canvas.show();
          render();
          /*
          painting = true;
          anchorX = (e.offsetX) ? e.offsetX : e.originalEvent.layerX;
          anchorY = (e.offsetY) ? e.offsetY : e.originalEvent.layerY;

          // Paints a redundant line of 0px length - but saves a few duplicate lines of code
          paintBaseline(e);
          */
        },

        clearSelection = function() {
           ctx.clearRect(0, 0, canvas.width, canvas.height);
        },

        setEnabled = function(enabled) {
          if (enabled) startPainting();
          else canvas.hide();
        };

    updateSize();

    // TODO needs to register & deregister in .setEnabled!
    canvas.on('mousemove', onMouseMove);
    canvas.on('click', onMouseClick);

    // Reset canvas on window resize
    jQuery(window).on('resize', updateSize);


    this.drawCursor = drawDot;

    this.clearSelection = clearSelection;
    this.setEnabled = setEnabled;
    this.updateSize = updateSize;

    Layer.apply(this, [ olMap ]);
  };
  Rect2DrawingTool.prototype = Object.create(Layer.prototype);

  return Rect2DrawingTool;

});
