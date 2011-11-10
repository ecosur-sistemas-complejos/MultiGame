/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.component {
	
	import flash.display.Shape;
	import flash.filters.DropShadowFilter;
	import flash.text.TextField;
	import flash.text.TextFieldAutoSize;
	import flash.text.TextFormat;
	
	import mx.core.UIComponent;
	import mx.ecosur.multigame.enum.Color;
	import mx.effects.AnimateProperty;

	public class GameStatus extends UIComponent {
		
		private var _bg:Shape;
		protected var _txtField:TextField;
		protected var _message:String;
		protected var _color:uint;
		protected var _active:Boolean;
		
		private const PADDING:Number = 10;
		private const BORDER_THICKNESS:Number = 2;
		private const CORNER_SIZE:Number = 10;
		private const SHADOW_SIZE:Number = 5;

		public function GameStatus() {
			super();
			_message = null;
			_active = true;
		}
		
		public function get active():Boolean{
			return active;
		}
		
		public function set active(active:Boolean):void{
			_active = active;
		}
		
		public function showMessage(message:String, color:uint):void{
			if (!_active){
				return;
			}
			_message = message;
			_txtField.text = _message;
			_color = color;
			invalidateSize();
			invalidateDisplayList();
			flashMessage();
		}
		
		private function flashMessage():void{
			if (!_active){
				return;
			}
			var ap:AnimateProperty = new AnimateProperty(this);
			ap.property = "alpha";
			ap.fromValue = 0;
			ap.toValue = 1;
			ap.duration = 1000;
			ap.play();
		}
				
		override protected function createChildren():void{
			
			//create background
			_bg = new Shape();
			addChild(_bg);
			
			//create text field
			_txtField = new TextField();
			_txtField.wordWrap = true;
			_txtField.autoSize = TextFieldAutoSize.CENTER;
			_txtField.x = PADDING;
			_txtField.y = PADDING;
			
			//create text format
			var txtFormat:TextFormat = new TextFormat();
			txtFormat.font = "Verdana";
			txtFormat.size = 10;
			txtFormat.bold = true;
			_txtField.defaultTextFormat = txtFormat;
			
			addChild(_txtField);
		}
				
		override protected function measure():void{
			
			_txtField.width = unscaledWidth - PADDING * 2;
			measuredHeight = _txtField.textHeight + 2 * PADDING;
			measuredMinHeight = measuredHeight;
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
			
			//draw background
			_bg.graphics.clear();
			_bg.graphics.beginFill(Color.findIntermediateColor(_color, 0xffffff, 0.9), 1);
			_bg.graphics.lineStyle(BORDER_THICKNESS, _color, 1); 
			_bg.graphics.drawRoundRect(0, y, unscaledWidth, measuredHeight, CORNER_SIZE, CORNER_SIZE);
			_bg.graphics.endFill();
			
			//add shadow filter if not present
			if (_bg.filters == null || _bg.filters.length == 0){
				var shadow:DropShadowFilter = new DropShadowFilter();
				shadow.distance = SHADOW_SIZE;
				shadow.color = 0x000000;
				shadow.angle = 90;
				_bg.filters = [shadow];
			}					
			
		}
		
	}
}
