package mx.ecosur.multigame.manantiales
{
	import flash.filters.DropShadowFilter;
	
	import mx.ecosur.multigame.component.BoardCell;
	import mx.ecosur.multigame.enum.Color;
	
	public class RoundCell extends BoardCell
	{		
		public function RoundCell(row:int, column:int, bgColor:uint, borderColor:uint, borderThickness:Number)
		{
			super(row, column, bgColor, borderColor, borderThickness);
			this.filters.concat(new DropShadowFilter());
		}
		
		public function get color ():String {
			var ret:String;
			
            if (column == 4) {
                ret = Color.UNKNOWN;
            } else if (row == 4) {
                ret = Color.UNKNOWN;
            } else if (column < 4 && row < 4) {
                ret = Color.BLUE;
            } else if (column < 4 && row > 4) {
                ret = Color.RED;
            } else if (column > 4 && row < 4) {
                ret = Color.BLACK;
            } else if (column > 4 && row > 4) {
                ret = Color.YELLOW;
            }			
            
            return ret;
		}
		
		/*
		  Override how cells are drawn, we use drawCircle as opposed to drawRect.
		*/
		override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void {

            super.updateDisplayList(unscaledWidth, unscaledHeight);
            
            //redraw bg
            _bg.graphics.clear();
            _bg.graphics.beginFill(_bgColor, 1);
            if (_borderThickness > 0){
                _bg.graphics.lineStyle(_borderThickness, _borderColor, 1);
            }
            /* We use ellipse to draw the rounded representation */
            _bg.graphics.drawEllipse(0, 0, unscaledWidth, unscaledHeight);
            _bg.graphics.endFill();
            _bg.filters = _bgFilters;
            
            //center and resize background image if present
            if(_bgImage){
                _bgImage.x = unscaledWidth / 2;
                _bgImage.y = unscaledHeight / 2;
                _bgImage.width = unscaledWidth - 2 * getStyle("padding");
                _bgImage.height = unscaledHeight - 2 * getStyle("padding");
            }
            
            //define size of token acording the the size of this
            if (_token){
                _token.width = unscaledWidth - 2 * getStyle("padding");
                _token.height = unscaledHeight - 2 * getStyle("padding");
                _token.x = unscaledWidth / 2;
                _token.y = unscaledHeight / 2;
            }
        }
		
	}
}