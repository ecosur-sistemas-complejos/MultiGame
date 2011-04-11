/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.enum {
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;

/**
	 * Enumeration of colors and related static helper classes.
	 */
	[ResourceBundle("Commons")]
	public class Color {
		
		public static const BLACK:String = "BLACK";
		public static const YELLOW:String = "YELLOW";
		public static const RED:String = "RED";
		public static const GREEN:String = "GREEN";
		public static const BLUE:String = "BLUE";
		public static const PURPLE:String = "PURPLE";
		public static const UNKNOWN:String = "UNKNOWN";
		
		//icon assets
		[Embed(source='/assets/icons.swf#cellIconRED')]
  		private static var iconRED:Class;
  		[Embed(source='/assets/icons.swf#cellIconBLACK')]
  		private static var iconBLACK:Class;
  		[Embed(source='/assets/icons.swf#cellIconYELLOW')]
  		private static var iconYELLOW:Class;
  		[Embed(source='/assets/icons.swf#cellIconBLUE')]
  		private static var iconBLUE:Class;
  		[Embed(source='/assets/icons.swf#cellIconGREEN')]
  		private static var iconGREEN:Class;
  		[Embed(source='/assets/icons.swf#cellIconPURPLE')]
  		private static var iconPURPLE:Class;
		
		public static function getColorCode(color:String):uint{
			switch (color){
                case Color.BLACK:
                    return 0x333333;
                case Color.YELLOW:
					return 0xffcc00;
                case Color.GREEN:
                    return 0x00bb00;
                case Color.BLUE:
					return 0x0000bb;
                case Color.RED:
					return 0xbb0000;
                case Color.PURPLE:
                    return 0x5F04B4;
            }
            return undefined;
		}
		
        public static function getColorDescription(color:String):String{
            var resource:IResourceManager = ResourceManager.getInstance();

            switch (color){
                case Color.BLACK:
                    return resource.getString("Commons", "color.black");
                case Color.YELLOW:
                    return resource.getString("Commons", "color.yellow");
                case Color.GREEN:
                    return resource.getString("Commons", "color.green");
                case Color.BLUE:
                    return resource.getString("Commons", "color.blue");
                case Color.RED:
                    return resource.getString("Commons", "color.red");
                case Color.PURPLE:
                    return resource.getString("Commons", "color.purple");
            }
            return "";
        }
        
        public static function getCellIcon(color:String):Class{
            switch (color){
                case Color.BLACK:
                    return iconBLACK;
                case Color.YELLOW:
                    return iconYELLOW;
                case Color.GREEN:
                    return iconGREEN;
                case Color.BLUE:
                    return iconBLUE;
                case Color.RED:
                    return iconRED;
                case Color.PURPLE:
                    return iconPURPLE;
            }
        return null;
        }

		public static function getCellIconSize():Number{
			//TODO: Look for a better way to read the height of the assets dynamically
			return 20;
		}
		
		public static function getTeamName(color:String):String{
			if (color == Color.YELLOW || color == Color.RED){
				return "HOT";
			}else if (color == Color.BLUE || color == Color.GREEN) {
				return "COLD";
			}
			return "";
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
