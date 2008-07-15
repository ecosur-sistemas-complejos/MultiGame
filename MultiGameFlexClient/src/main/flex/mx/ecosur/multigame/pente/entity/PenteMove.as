package mx.ecosur.multigame.pente.entity {
	
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.Move;

	/**
	 * Represents a server side PenteMove object.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.pente.PenteMove")]
	public class PenteMove extends Move {
		
		private var _captures:ArrayCollection;
		
		public function PenteMove() {
			super();
		}
				
		public function set captures(captures:ArrayCollection):void{
			_captures = captures;
		}
		
		public function get captures():ArrayCollection{
			return _captures;
		}
		
	}
}
