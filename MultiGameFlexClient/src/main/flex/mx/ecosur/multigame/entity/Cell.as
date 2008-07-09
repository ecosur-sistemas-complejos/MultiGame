package mx.ecosur.multigame.entity {
	
	import flash.display.Shape;
	import flash.filters.BevelFilter;
	import flash.filters.BitmapFilterQuality;
	import flash.filters.BitmapFilterType;
	import flash.filters.DropShadowFilter;
	import flash.filters.GlowFilter;
	import flash.filters.GradientBevelFilter;
	
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	
	import mx.ecosur.multigame.Color;
	import mx.ecosur.helper.ColorUtils;
	
	[RemoteClass (alias="mx.ecosur.multigame.Cell")]
	public class Cell extends UIComponent{
		
		private var _bg:Shape;
		private var _row:int;
		private var _column:int;
		private var _color:String;
		private var _colorCode:uint;
		private var _player:Player;
		
		//Constants
		public static const DEFAULT_ALPHA:Number = 0.8;
		public static const DISACTIVATED_ALPHA:Number = 0.5;
		public static const HIGHLIGHT_ALPHA:Number = 1;
		public static const BORDER_THICKNESS:Number = 1;
		
		public function Cell(){
			super();
			alpha = DEFAULT_ALPHA;

		}
		
		public function get row():int{
			return _row;
		}
		
		public function set row(row:int):void {
			_row = row;
		}
		
		public function get column():int{
			return _column;
		}
		
		public function set column(column:int):void {
			_column = column;
		}
		
		public function get color():String{
			return _color;
		}
		
		public function set color(color:String):void{
			if (color != _color){
				_colorCode = Color.getColorCode(color);
				_color = color;
				invalidateDisplayList();
			}
		}
		
		public function get colorCode():uint{
			return _colorCode;
		}
		
		public function get player():Player{
			return _player;
		}
		
		public function set player(player:Player):void{
			_player = player;
		}
		
		public function createDragImage():IFlexDisplayObject{
			var dragImage:Cell = new Cell();
			dragImage.color = _color;
			dragImage.width = width;
			dragImage.height = height;
			return IFlexDisplayObject(dragImage);
		}
		
		public function clone():Cell{
			var clone:Cell = new Cell();
			clone.color = _color;
			clone.column = _column;
			clone.row = _row;
			clone.player = _player;
			return clone; 
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
    		_bg.x = - unscaledWidth / 2;
    		_bg.y = - unscaledHeight / 2;
    		_bg.graphics.beginFill(_colorCode, 1);
    		_bg.graphics.lineStyle(BORDER_THICKNESS, _colorCode, 1);
    		_bg.graphics.drawCircle(unscaledWidth/2, unscaledHeight/2, unscaledWidth / 2);
    		_bg.graphics.endFill();
    		
    		var filters:Array = new Array();
    		
    		//add shadow filter
    		var shadow:DropShadowFilter = new DropShadowFilter();
			shadow.distance = 5;
			shadow.angle = 90;
			//filters.push(shadow);
			
			//add bevel filter
			var bevel:BevelFilter = new BevelFilter();
			bevel.distance = 1;
			bevel.angle = 45;
			bevel.highlightColor = 0xff0000;
			bevel.highlightAlpha = 0.8;
			bevel.shadowColor = 0x00ff00;
			bevel.shadowAlpha = 0.8;
			bevel.blurX = 5;
			bevel.blurY = 5;
			bevel.strength = 5;
			bevel.quality = BitmapFilterQuality.HIGH;
			bevel.type = BitmapFilterType.INNER;
			bevel.knockout = false;
			//filters.push(bevel);
			
			var gradientBevel:GradientBevelFilter = new GradientBevelFilter();
			gradientBevel.distance = 8;
			gradientBevel.angle = 225; // opposite of 45 degrees
			//TODO find intermediate colors
			gradientBevel.colors = [ColorUtils.findIntermediateColor(0xffffff, colorCode), colorCode, ColorUtils.findIntermediateColor(0x000000, colorCode)];
			gradientBevel.alphas = [1, 0, 1];
			gradientBevel.ratios = [0, 128, 255];
			gradientBevel.blurX = 8;
			gradientBevel.blurY = 8;
			gradientBevel.quality = BitmapFilterQuality.HIGH;
			filters.push(gradientBevel);
			
			var glow:GlowFilter = new GlowFilter();
			glow.color = 0x009922;
			glow.alpha = 1;
			glow.blurX = 25;
			glow.blurY = 25;
			glow.quality = BitmapFilterQuality.MEDIUM;
			//filters.push(glow);

			_bg.filters = filters;

        }
	}
}