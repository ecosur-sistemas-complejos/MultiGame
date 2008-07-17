package mx.ecosur.multigame.component {
	
	import flash.display.Shape;
	import flash.filters.BitmapFilterQuality;
	import flash.filters.GlowFilter;
	
	import mx.ecosur.multigame.component.Token;
	
	import mx.core.UIComponent;
	
	[Style(name="padding", type="Number", format="Length")]

	/**
	 * Represents a cell on the board.
	 */
	public class BoardCell extends UIComponent {
		
		private var _bg:Shape;
		private var _token:Token;
		private var _row:int;
		private var _column:int;
		
		/* colors */
		private var _bgColor:uint;
		private var _borderColor:uint;
		private var _borderThickness:Number;
		
		/* default colors */
		private var _defaultBgColor:uint;
		private var _defaultBorderColor:uint;
		private var _defaultBorderThickness:Number;
		
		/* selected move colors */
		private var _selectedBgColor:uint;
		private var _selectedBorderColor:uint;
		private var _selectedBorderThickness:Number;
		
		/* filters for background */
		private var _bgFilters:Array;
		private var _bgDefaultFilters:Array;
		private var _bgSelectedFilters:Array;
		
		/**
		 * Constructor
		 *  
		 * @param row the row number, 0 indicates the top row
		 * @param column the column number, 0 indicates the left most column 
		 * @param bgColor a hexidecimal representation of the background color
		 * @param borderColor a hexidecimal representation of the the border color
		 * @param borderThickness the thickness, in pixels, of the border
		 * 
		 */
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
		
		/* Getters and setters */
		
		public function get token():Token{
			return _token;
		}
		
		public function set token(token:Token):void{
			if (_token != null){
				removeChild(_token);
			}
			if (token != null){
				token.cell.row = _row;
				token.cell.column = _column;
				addChild(token);
				invalidateDisplayList();
			}
			_token = token;
		}
		
		public function get row():int{
			return _row;
		}

		public function get column():int{
			return _column;
		}		
		
		/**
		 * Visualy selects the board cell.
		 *  
		 * @param color the selected color
		 */
		public function select(color:uint):void{
			
			/* change filters */
			var glow:GlowFilter = GlowFilter(_bgSelectedFilters[0]);
			glow.color = color;
			_bgFilters = _bgSelectedFilters;
			_bg.filters = _bgFilters;
			
		}
		
		/**
		 * Visualy resets the board cell after selection.
		 *  
		 */
		public function reset():void{
			
			/* change filters */
			_bgFilters = _bgDefaultFilters;
			_bg.filters = _bgFilters;
			invalidateDisplayList();
		}
		
		/* Overrides */
		
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
