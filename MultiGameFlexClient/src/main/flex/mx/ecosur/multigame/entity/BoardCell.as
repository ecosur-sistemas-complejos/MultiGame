package mx.ecosur.multigame.entity {
	
	import flash.display.Shape;
	import flash.filters.BitmapFilterQuality;
	import flash.filters.GlowFilter;
	
	import mx.core.UIComponent;
	import mx.controls.Alert;
	
	[Style(name="padding", type="Number", format="Length")]

	public class BoardCell extends UIComponent
	{
		
		private var _bg:Shape;
		private var _cell:Cell;
		private var _row:int;
		private var _column:int;
		
		//colors
		private var _bgColor:uint;
		private var _borderColor:uint;
		private var _borderThickness:Number;
		
		//default colors
		private var _defaultBgColor:uint;
		private var _defaultBorderColor:uint;
		private var _defaultBorderThickness:Number;
		
		//selected move colors
		private var _selectedBgColor:uint;
		private var _selectedBorderColor:uint;
		private var _selectedBorderThickness:Number;
		
		//filters for background
		private var _bgFilters:Array;
		private var _bgDefaultFilters:Array;
		private var _bgSelectedFilters:Array;
		
		public function BoardCell(row:int, column:int, bgColor:uint, borderColor:uint, borderThickness:Number){
			super();
			
			//define position in board
			_row = row;
			_column = column;
			
			//define colors
			_bgColor = bgColor;
			_borderColor = borderColor;
			_borderThickness = borderThickness;
			_defaultBgColor = bgColor;
			_defaultBorderColor = borderColor;
			_defaultBorderThickness = borderThickness;
			_selectedBgColor = bgColor;
			_selectedBorderColor = borderColor;
			_selectedBorderThickness = 5;
			
			//initialize filters
			_bgDefaultFilters = new Array();
			_bgSelectedFilters = new Array(); 
			var glow:GlowFilter = new GlowFilter();
			glow.color = 0xffffff;
			glow.alpha = 1;
			glow.blurX = 10;
			glow.blurY = 10;
			glow.inner = true;
			glow.quality = BitmapFilterQuality.MEDIUM;
			_bgSelectedFilters.push(glow);
			
		}
		
		public function get cell():Cell{
			return _cell;
		}
		
		public function set cell(cell:Cell):void{
			if (_cell != null){
				removeChild(_cell);
			}
			if (cell != null){
				cell.row = _row;
				cell.column = _column;
				addChild(cell);
			}
			_cell = cell;
		}
		
		public function get row():int{
			return _row;
		}

		public function get column():int{
			return _column;
		}		
		
		public function select(color:uint):void{
			
			//move cell to top of children
			//parent.swapChildrenAt(parent.numChildren - 1, parent.getChildIndex(this));
			
			//change filters
			var glow:GlowFilter = GlowFilter(_bgSelectedFilters[0]);
			glow.color = color;
			_bgFilters = _bgSelectedFilters;
			_bg.filters = _bgFilters;
			
		}
		
		public function reset():void{
			
			//move cell to bottom of children
			//parent.swapChildrenAt(0, parent.getChildIndex(this));
			
			//change filters
			_bgFilters = _bgDefaultFilters;
			_bg.filters = _bgFilters;
			invalidateDisplayList();
		}
		
		override protected function createChildren():void{
			_bg = new Shape();
			addChild(_bg);
		}
		
		override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void {

            super.updateDisplayList(unscaledWidth, unscaledHeight);
    		
    		//redraw bg
    		_bg.graphics.clear();
    		_bg.graphics.beginFill(_bgColor, 1);
    		if (_borderThickness > 0){
    			_bg.graphics.lineStyle(_borderThickness, _borderColor, 1);
    		}
    		_bg.graphics.drawRect(0, 0, unscaledWidth, unscaledHeight);
    		_bg.graphics.endFill();
    		_bg.filters = _bgFilters;
    		
    		//define size of token by size of cell
    		if (_cell){
    			_cell.width = unscaledWidth - getStyle("padding");
    			_cell.height = unscaledHeight - getStyle("padding");
    			_cell.x = unscaledWidth / 2;
    			_cell.y = unscaledHeight / 2;
    		}
        }
        
	}
}