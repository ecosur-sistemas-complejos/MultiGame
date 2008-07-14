package mx.ecosur.multigame.entity.pente {
	
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.Player;
	
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