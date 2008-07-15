package mx.ecosur.multigame.pente.dto {
	
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
