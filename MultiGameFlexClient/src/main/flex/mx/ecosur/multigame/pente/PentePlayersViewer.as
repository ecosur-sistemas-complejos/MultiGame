package mx.ecosur.multigame.pente {
	
	import mx.collections.ArrayCollection;
	import mx.containers.Accordion;
	import mx.controls.Button;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.GamePlayer;
	import mx.ecosur.multigame.helper.Color;
	
	/**
	 * Visual component representing the players that are in the current
	 * game.
	 * 
	 */
	public class PentePlayersViewer extends Accordion {
		
		private var _players:ArrayCollection; 
		private var _currentPlayer:GamePlayer;
		
		/**
		 * Default constructor 
		 */
		public function PentePlayersViewer(){
			super();
		}
		
		/* Getters and setters */
		
		public function get players():ArrayCollection{
			return _players;
		}
		
		public function set players(players:ArrayCollection):void{
			_players = players;
			updatePlayers();
		}
		
		//TODO: Avoid calling create players twice when players and currentPlayer are set
		public function set currentPlayer(currentPlayer:GamePlayer):void{
			//Alert.show("setting current player " + currentPlayer.color);
			_currentPlayer = currentPlayer;
			updatePlayers();
		}
		
		/*
		 * Updates the players and opens the player whose turn it is. 
		 */
		private function updatePlayers():void{
			
			var ppi:PentePlayerInfo;
			var btn:Button;
			var cell:Cell;
			var gamePlayer:GamePlayer;
			var i:int;
			
			for (i = 0; i < _players.length; i++){
				
				/* Create the player information */
				gamePlayer = GamePlayer(_players[i]);
				if (getChildren().length > i) {
					ppi = PentePlayerInfo(getChildAt(i));
				}else{
					ppi = new PentePlayerInfo();
					addChild(ppi);
				}
				ppi.gamePlayer = gamePlayer;
				
				/* Create button header */
				btn = getHeaderAt(i);
				btn.label = gamePlayer.player.name;
				btn.setStyle("icon", Color.getCellIcon(gamePlayer.color));
				btn.setStyle("paddingBottom", 5);
				btn.setStyle("paddingTop", 5);
				if (gamePlayer.turn){
					var fillColor:uint;
					fillColor = Color.findIntermediateColor(Color.getColorCode(gamePlayer.color), 0x000000, 0.5);
					btn.setStyle("fillColors", [fillColor, 0xffffff]);
					btn.setStyle("fillAlphas", [1, 1]);
					btn.setStyle("borderColor", fillColor);
					selectedChild = ppi;
				}else{
					btn.setStyle("fillColors", [0xE6EEEE, 0xFFFFFF]);
					btn.setStyle("fillAlphas", [0.6, 0.4]);
					btn.setStyle("borderColor", 0xAAB3B3);
				}
			}
			
			/* remove extra children that are no longer used */
			while (i < getChildren().length){
				removeChildAt(i);	
			}
		}
		
	}
		
}