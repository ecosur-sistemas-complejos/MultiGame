package mx.ecosur.multigame.pente {
	
	import mx.collections.ArrayCollection;
	import mx.containers.Accordion;
	import mx.controls.Button;
	import mx.ecosur.multigame.Color;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.Player;
	import mx.controls.Alert;
	
	public class PentePlayersViewer extends Accordion {
		
		private var _players:ArrayCollection; 
		private var _currentPlayer:Player;
		
		public function PentePlayersViewer(){
			super();
		}
		
		public function get players():ArrayCollection{
			return _players;
		}
		
		public function set players(players:ArrayCollection):void{
			_players = players;
			removeAllChildren();
			createPlayers();
		}
		
		//TODO: Avoid calling create players twice when players and principal are set
		public function set currentPlayer(currentPlayer:Player):void{
			//Alert.show("setting current player " + currentPlayer.color);
			_currentPlayer = currentPlayer;
			removeAllChildren();
			createPlayers();
		}
		
		private function createPlayers():void{
			var ppi:PentePlayerInfo;
			var btn:Button;
			var cell:Cell;
			var player:Player;
			for (var i:int = 0; i < _players.length; i++){
				player = Player(_players[i]);
				ppi = new PentePlayerInfo();
				addChild(ppi);
				ppi.player = player;
				btn = getHeaderAt(i);
				btn.label = player.name;
				btn.setStyle("icon", Color.getCellIcon(player.color));
				btn.setStyle("paddingBottom", 5);
				btn.setStyle("paddingTop", 5);
				if (player.turn){
					var fillColor:uint;
					if (player.id == _currentPlayer.id){
						fillColor = 0x33cc00;
					}else{
						fillColor = 0xff8000;
					}
					btn.setStyle("fillColors", [fillColor, 0xffffff]);
					btn.setStyle("fillAlphas", [1, 1]);
					btn.setStyle("borderColor", fillColor);
					selectedChild = ppi;
				}
			}
		}
		
	}
		
}