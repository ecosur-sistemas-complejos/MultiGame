package mx.ecosur.multigame {
	import mx.ecosur.multigame.entity.Cell;
	
	public class Color {
		
		public static const BLACK:String = "BLACK";
		public static const RED:String = "RED";
		public static const GREEN:String = "GREEN";
		public static const BLUE:String = "BLUE";
		public static const UNKNOWN:String = "UNKNOWN";
		
		//icon assets
		[Embed(source='assets/icons.swf#cellIconRED')]
  		private static var iconRED:Class;
  		[Embed(source='assets/icons.swf#cellIconBLACK')]
  		private static var iconBLACK:Class;
  		[Embed(source='assets/icons.swf#cellIconBLUE')]
  		private static var iconBLUE:Class;
  		[Embed(source='assets/icons.swf#cellIconGREEN')]
  		private static var iconGREEN:Class;
		
		public static function getColorCode(color:String):uint{
			switch (color){
				case Color.BLACK:
					return 0x000000;
				break;
				case Color.GREEN:
					return 0x00ff00;
				break;
				case Color.BLUE:
					return 0x0000ff;
				break;
				case Color.RED:
					return 0xff0000;
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

	}
}