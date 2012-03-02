package mx.ecosur.multigame.manantiales.token {
import mx.ecosur.multigame.manantiales.token.ManantialesToken;

public class UndevelopedToken extends ManantialesToken {
        
        public function UndevelopedToken () {
            super ();
        }
        
        override public function get colorCode():uint {
            return 0x00bb00;
        }

        override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void 
       {
            // Redraw background
            _bg.graphics.clear();
            _bg.alpha = 0.50;
            _bg.x = - unscaledWidth / 2;
            _bg.y = - unscaledHeight / 2;
            _bg.graphics.beginFill(colorCode, 1);
            _bg.graphics.lineStyle(BORDER_THICKNESS, colorCode, 1);
            _bg.graphics.drawCircle(unscaledWidth/2, unscaledHeight/2, unscaledWidth / 2);
            _bg.graphics.endFill();
            _bgDirty = false;
        
            // Set filters acording to whether the token is selected or not
            if (_selected){
                _bg.filters = _selectedFilters;
            }else{
                _bg.filters = _deselectedFilters;
            }           
        } 
    }
}
