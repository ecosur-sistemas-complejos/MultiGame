/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

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