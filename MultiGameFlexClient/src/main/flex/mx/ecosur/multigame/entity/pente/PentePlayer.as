package mx.ecosur.multigame.entity.pente {
	
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.GamePlayer;
	
	[RemoteClass (alias="mx.ecosur.multigame.ejb.entity.pente.PentePlayer")]
	public class PentePlayer extends GamePlayer{
		
		private var _points:int;
		private var _trias:ArrayCollection;
		private var _tesseras:ArrayCollection;
		
		public function PentePlayer(){
			super();
		}
		
		public function get pints():int{
			return _points;
		}
		
		public function set points(points:int):void {
			_points = points;
		}
		
		public function get trias():ArrayCollection{
			return _trias;
		}
		
		public function set trias(trias:ArrayCollection):void {
			_trias = trias;
		}
		
		public function get tesseras():ArrayCollection{
			return _tesseras;
		}
		
		public function set tesseras(tesserras:ArrayCollection):void {
			_tesseras = tesseras;
		}
	}
}