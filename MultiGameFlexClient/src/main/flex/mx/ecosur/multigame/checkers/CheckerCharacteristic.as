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
	import mx.ecosur.multigame.entity.Characteristic;

	[RemoteClass (alias="mx.ecosur.multigame.checkers.CheckerCharacteristic")]
	public class CheckerCharacteristic implements Characteristic {
	
		private var _kinged:Boolean;

		public function CheckerCharacteristic () {
			super();
			_kinged = false;
		}
	
		public function get kinged():Boolean {
			return _kinged;
		}

		public function set kinged(kinged:Boolean):void {
			_kinged = kinged;
		}
	}
}