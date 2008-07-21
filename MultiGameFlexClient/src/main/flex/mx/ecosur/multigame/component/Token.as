package mx.ecosur.multigame.component {
	
	import flash.display.Shape;
	import flash.filters.BitmapFilterQuality;
	import flash.filters.GlowFilter;
	import flash.filters.GradientBevelFilter;
	
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.enum.Color;
	
	/**
	 * Visual class representing a token on the board. Contains an internal 
	 * cell object that represents a server side Cell.
	 */
	public class Token extends UIComponent{
		
		private var _cell:Cell;
		private var _bg:Shape;
		private var _selected:Boolean;
		private var _deselectedFilters:Array;
		private var _selectedFilters:Array;
		
		//flags
		private var _bgDirty:Boolean;
		
		// Constants
		public static const BORDER_THICKNESS:Number = 1;
		
		/**
		 * Default constructor. Initializes token with DEFAULT_ALPHA
		 * 
		 */
		public function Token(){
			super();
			_selected = false;
			_bgDirty = false;
			
			
		}
		
		/* Getters and setters */

		public function get cell():Cell{
			return _cell;
		}
		
		public function set cell(cell:Cell):void {
			if (_cell == null || _cell.colorCode != cell.colorCode){
				
				//initialize filters
				_deselectedFilters = new Array();
				_selectedFilters = new Array();			
				
				var glow:GlowFilter = new GlowFilter();
				glow.color = 0xffffff;
				glow.alpha = 1;
				glow.blurX = 5;
				glow.blurY = 5;
				glow.inner = false;
				glow.quality = BitmapFilterQuality.MEDIUM;
				_selectedFilters.push(glow);
				
				var gradientBevel:GradientBevelFilter = new GradientBevelFilter();
				gradientBevel.distance = 8;
				gradientBevel.angle = 225; // opposite of 45 degrees
				gradientBevel.colors = [Color.findIntermediateColor(0xffffff, cell.colorCode, 0.5), cell.colorCode, Color.findIntermediateColor(0x000000, cell.colorCode, 0.5)];
				gradientBevel.alphas = [1, 0, 1];
				gradientBevel.ratios = [0, 128, 255];
				gradientBevel.blurX = 8;
				gradientBevel.blurY = 8;
				gradientBevel.quality = BitmapFilterQuality.HIGH;
				_deselectedFilters.push(gradientBevel);
				_selectedFilters.push(gradientBevel);
				
				_bgDirty = true;
				invalidateDisplayList();
			}
			_cell = cell;
		}
		
		public function set selected(selected:Boolean):void{
			if (_selected != selected){
				_selected = selected;
				invalidateDisplayList();
			}
		}
		
		/**
		 * Creates a display object that is visually the same as the Token
		 * to be used as a drag image.
		 *  
		 * @return the drag image
		 */
		public function createDragImage():IFlexDisplayObject{
			var dragImage:Token = new Token();
			dragImage.cell = _cell;
			dragImage.width = width;
			dragImage.height = height;
			return IFlexDisplayObject(dragImage);
		}
		
		/**
		 * Creates a clone of the token copying the internal cell data
		 *  
		 * @return the clone
		 */
		public function clone():Token{
			var clone:Token = new Token();
			clone.cell = _cell;
			return clone; 
		}
		
		/* Overrides */
				
		override protected function createChildren():void{
			_bg = new Shape();
			addChild(_bg);
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
    		_bg.graphics.beginFill(_cell.colorCode, 1);
    		_bg.graphics.lineStyle(BORDER_THICKNESS, _cell.colorCode, 1);
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
        
		override public function toString():String{
			return "cell = {" + cell + "}"; 
		}
	}
}