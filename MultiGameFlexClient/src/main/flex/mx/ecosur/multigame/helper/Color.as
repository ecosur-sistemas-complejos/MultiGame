package mx.ecosur.multigame.helper {
	
	/**
	 * Enumeration of colors and related static helper classes.
	 */
	public class Color {
		
		public static const BLACK:String = "BLACK";
		public static const RED:String = "RED";
		public static const GREEN:String = "GREEN";
		public static const BLUE:String = "BLUE";
		public static const UNKNOWN:String = "UNKNOWN";
		
		//icon assets
		[Embed(source='/assets/icons.swf#cellIconRED')]
  		private static var iconRED:Class;
  		[Embed(source='/assets/icons.swf#cellIconBLACK')]
  		private static var iconBLACK:Class;
  		[Embed(source='/assets/icons.swf#cellIconBLUE')]
  		private static var iconBLUE:Class;
  		[Embed(source='/assets/icons.swf#cellIconGREEN')]
  		private static var iconGREEN:Class;
		
		public static function getColorCode(color:String):uint{
			switch (color){
				case Color.BLACK:
					return 0x000000;
				break;
				case Color.GREEN:
					return 0x00bb00;
				break;
				case Color.BLUE:
					return 0x0000bb;
				break;
				case Color.RED:
					return 0xbb0000;
				break;
			}
			return undefined;
		}
		
		public static function getCellIcon(color:String):Class{
			switch (color){
				case Color.BLACK:
					return iconBLACK;
				break;
				case Color.GREEN:
					return iconGREEN;
				break;
				case Color.BLUE:
					return iconBLUE;
				break;
				case Color.RED:
					return iconRED;
				break;
			}
			return null;
		}
		
		public static function findIntermediateColor(color1:uint, color2:uint, ratio:Number):uint{
			var r:uint = color1 >> 16;
			var g:uint = color1 >> 8 & 0xff;
			var b:uint = color1 & 0xff;
			r += ((color2 >> 16) - r) * ratio;
			g += ((color2 >> 8 & 0xff) - g) * ratio;
			b += ((color2 & 0xff) - b) * ratio;
			return (r << 16 | g << 8 | b);
		}
		
		public static function uint2Str(color:uint):String{
			var s:String = "0x";
			s += (color >> 16).toString(16);
			s += (color >> 8 & 0xff).toString(16);
			s += (color & 0xff).toString(16);
			return s;
		}

	}
}
