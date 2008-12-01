/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.pente.entity {
	
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.Game;

	/**
	 * Represents a server side PenteGame object. 
	 */
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.pente.PenteGame")]
	public class PenteGame extends Game {
		
		private var _winners:ArrayCollection;
		
		public function PenteGame() {
			super();
		}
		
		public function get winners():ArrayCollection{
			return _winners;
		}
		
		public function set winners(winners:ArrayCollection):void{
			_winners = winners;
		}	
		
		override public function toString():String{
			return super.toString() + " winners = {" + winners + "}";
		}
	}
}