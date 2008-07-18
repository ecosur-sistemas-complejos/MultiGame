package mx.ecosur.multigame.pente.entity {
	
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.Move;

	/**
	 * Represents a server side PenteMove object.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.pente.PenteMove")]
	public class PenteMove extends Move {
		
		private var _captures:ArrayCollection;
		
		private var _trias:ArrayCollection;
		
		private var _tesseras:ArrayCollection;
		
		public function PenteMove() {
			super();
		}
				
		public function set captures(captures:ArrayCollection):void{
			_captures = captures;
		}
		
		public function get captures():ArrayCollection{
			return _captures;
		}
		
		public function set trias(trias:ArrayCollection):void{
			_trias = trias;
		}
		
		public function get trias():ArrayCollection{
			return _trias;
		}
		
		public function set tesseras(tesseras:ArrayCollection):void{
			_tesseras = tesseras;
		}
		
		public function get tesseras():ArrayCollection{
			return _tesseras;
		}
		
	}
}
