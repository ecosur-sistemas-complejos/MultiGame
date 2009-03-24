package mx.ecosur.multigame.manantiales
{
    import mx.controls.Image;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.enum.Color;

	public class ManantialesToken extends Token
	{	
        protected var _label:String;
        protected var _tooltip:String;	
        protected var _colorCode:uint;
        protected var _bgImage:Image;
        protected var _territoryColor:Color;
		
		public function ManantialesToken()
		{
			super();
		}
		
		public function get territoryColor():Color {
			return this._territoryColor;
		}
		
		public function get colorCode():uint {
			return _cell.colorCode;
		}		
		
        override protected function createChildren():void{
        	super.createChildren();
        	if (_bgImage != null)
        	   addChild(_bgImage);  
            
            if (_tooltip != null)
                this.toolTip = _tooltip;
        }
        
        override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void {
                
            // Do nothing if color not set
            if (_cell == null || _cell.color == null){
                return;
            }
            
            // Redraw background
            _bg.graphics.clear();
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