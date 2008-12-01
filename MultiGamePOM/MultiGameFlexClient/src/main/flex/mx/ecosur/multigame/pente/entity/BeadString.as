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
	import mx.ecosur.multigame.entity.Player;
	
	/**
	 * Represents a server side BeadString object.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.pente.BeadString")]
	public class BeadString{
		
		private var _beads:ArrayCollection;
		
		public function BeadString(){
			super();
		}
		
		public function get beads():ArrayCollection{
			return _beads;
		}
		
		public function set beads(beads:ArrayCollection):void {
			_beads = beads;
		}
	}
}