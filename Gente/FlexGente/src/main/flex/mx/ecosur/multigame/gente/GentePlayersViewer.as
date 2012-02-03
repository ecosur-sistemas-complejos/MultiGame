/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.gente {
	
	import mx.collections.ArrayCollection;
	import mx.containers.Accordion;
	import mx.controls.Button;
	import mx.ecosur.multigame.enum.Color;
	import mx.ecosur.multigame.gente.entity.GentePlayer;
	
	/**
	 * Visual component representing the players that are in the current
	 * game.
	 * 
	 */
	public class GentePlayersViewer extends Accordion {
		
		private var _players:ArrayCollection; 
		private var _selectedPlayer:GentePlayerInfo;
		
		/**
		 * Default constructor 
		 */
		public function GentePlayersViewer(){
			super();
		}
		
		/**
		 * Sets players
		 *  
		 * @param players
		 */
		public function set players(players:ArrayCollection):void{
			_players = players;
			updatePlayers();
		}
		
		public function setTurn(player:GentePlayer):void{
			var ppi:GentePlayerInfo;
			for (var i:int = 0; i < getChildren().length; i++){
				ppi = GentePlayerInfo(getChildAt(i))
				if (ppi.pentePlayer.id == player.id){
					selectPlayer(ppi);
					return;
				}
			}
		}
		
		
		/**
		 * Returns the button for this player.
		 * 
		 * @param player
		 */
		public function getPlayerButton(player:GentePlayer):Button{
			
			var ppi:GentePlayerInfo;
			var btn:Button;
			for (var i:int = 0; i < getChildren().length; i++){
				ppi = GentePlayerInfo(getChildAt(i))
				if (ppi.pentePlayer.id == player.id){
					btn = getHeaderAt(i);
					return btn;
				}
			}
			return null;
		}
		
		/*
		 * Updates the players and opens the player whose turn it is. 
		 */
		private function updatePlayers():void{
			
			var ppi:GentePlayerInfo;
			var btn:Button;
			var player:GentePlayer;
			var i:int;
			
			for (i = 0; i < _players.length; i++){
				
				// Create the player information
				player = GentePlayer(_players[i]);
				if (getChildren().length > i) {
					ppi = GentePlayerInfo(getChildAt(i));
				}else{
					ppi = new GentePlayerInfo();
					addChild(ppi);
				}
				ppi.pentePlayer = player;
				
				// Create button header
				var label:String = player.name + " (" + Color.getTeamName(player.color) + ")";
				btn = getHeaderAt(i);
				btn.label = label;
				btn.setStyle("icon", Color.getCellIcon(player.color));
				btn.setStyle("paddingBottom", 5);
				btn.setStyle("paddingTop", 5);
				
				// If player has turn highlight and select its info
				if (player.turn){
					selectPlayer(ppi);
				}
			}
			
			// Remove extra children that are no longer used
			while (i < getChildren().length){
				removeChildAt(i);	
			}
		}
		
		private function selectPlayer(ppi:GentePlayerInfo):void{
			
			//deselect selected
			if (_selectedPlayer){
				var btn:Button = getHeaderAt(getChildIndex(_selectedPlayer));
				btn.setStyle("fillColors", [0xE6EEEE, 0xFFFFFF]);
				btn.setStyle("fillAlphas", [0.6, 0.4]);
				btn.setStyle("color", 0x0B333C);
				btn.setStyle("borderColor", 0xAAB3B3);
			}
			
			//select new
			btn = getHeaderAt(getChildIndex(ppi));
			btn.setStyle("fillColors", [0xFF3300, 0xFF6600]);
			btn.setStyle("fillAlphas", [1, 1]);
			btn.setStyle("color", 0xFFFFFF);
			btn.setStyle("borderColor", 0xFF3300);
			
			_selectedPlayer = ppi;
			selectedChild = ppi;
		}
		
		
	}
		
}
