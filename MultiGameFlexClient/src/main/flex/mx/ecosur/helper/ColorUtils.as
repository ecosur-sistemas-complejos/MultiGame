package mx.ecosur.helper {
	
	public class ColorUtils {
		
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