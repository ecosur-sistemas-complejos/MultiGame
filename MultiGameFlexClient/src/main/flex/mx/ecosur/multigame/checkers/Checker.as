package mx.ecosur.multigame.checkers
{
	import mx.ecosur.multigame.entity.Cell;

	[RemoteClass (alias="mx.ecosur.multigame.checkers.Checker")]
	public class Checker extends Cell
	{
		
		private var _characteristic:CheckerCharacteristic;
		
		public function Checker()
		{
			super();
		}
		
		public function get characteristic():CheckerCharacteristic{
			return _characteristic;
		}
		
		public function set characteristic(characteristic:CheckerCharacteristic):void{
			_characteristic = characteristic;
		}
		
	}
}