package mx.ecosur.multigame.manantiales
    {
    import flash.filters.DropShadowFilter;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.enum.Color;

public class RoundCell extends BoardCell
    {
        private var _factor:int;

        public function RoundCell(column:int, row:int,  bgColor:uint, borderColor:uint, borderThickness:Number)
        {
            super(column, row, bgColor, borderColor, borderThickness);
            this.filters.concat(new DropShadowFilter());
            _factor = 4;
        }

        public function get color ():String {
            var ret:String;

            if (column < 4 && row < 4)
                 ret = Color.YELLOW;
            else if (column < 4 && row > 4)
                 ret = Color.PURPLE;
            else if (column > 4 && row < 4)
                 ret = Color.RED;
            else if (column > 4 && row > 4)
                 ret = Color.BLACK;
            else
                ret = Color.UNKNOWN;

            return ret;
        }
        
        public function get tokenSize():int {
            return unscaledWidth + _factor
        }

        /*
          Override how cells are drawn, we use drawEllipse as opposed to drawRect.
        */
        override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void 
        {
            //redraw bg
            _bg.graphics.clear();
            _bg.graphics.beginFill(_bgColor, 1);
            if (_borderThickness > 0){
                _bg.graphics.lineStyle(_borderThickness, _borderColor, 1);
            }
            /* We use ellipse to draw the rounded representation */
            _bg.graphics.drawEllipse(0, 0, tokenSize, tokenSize);
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
                _token.x = (tokenSize) / 2;
                _token.y = (tokenSize) / 2;
            }
        }

        override public function set token(token:Token):void{
            if (_token != null)
                removeChild(_token);
            if (token != null){
                addChild(token);
                invalidateDisplayList();
                var manToken:ManantialesToken = ManantialesToken(token);
                manToken.placed = true;
            }
            
            _token = token;
        }
    }
}
