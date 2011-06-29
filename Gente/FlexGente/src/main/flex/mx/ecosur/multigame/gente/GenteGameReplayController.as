/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.gente{
	
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.core.UIComponent;
	import mx.ecosur.multigame.component.BoardCell;
	import mx.ecosur.multigame.component.Token;
	import mx.ecosur.multigame.entity.Move;
	import mx.ecosur.multigame.enum.Color;
	import mx.ecosur.multigame.entity.gente.GenteGame;
	import mx.ecosur.multigame.gente.entity.GenteMove;
	import mx.ecosur.multigame.entity.gente.GentePlayer;
	import mx.effects.AnimateProperty;
	import mx.events.DynamicEvent;
	import mx.events.EffectEvent;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;
	

	/**
	 * Replays a game of gente.
	 */
	public class GenteGameReplayController {
		
		// Visual components
		private var _board:GenteBoard;
		private var _playersViewer:GentePlayersViewer;
		private var _moveViewer:GenteMoveViewer;
		private var _animateLayer:UIComponent;
		
		// Data objects
        private var _gameId:int;
		private var _game:GenteGame;
		private var _moves:ArrayCollection; //all moves made in the game
		private var _selectedMoveInd:Number; //index of selected move in _moves
		
		// Server objects
		private var _gameService:RemoteObject;
		
		// constants
		private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
		private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
		private static const GAME_SERVICE_GET_GAME_OP:String = "getGame";
		private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
		private static const GAME_SERVICE_GET_MOVES_OP:String = "getMoves";
		
		/**
		 * Default constructor. 
		 * 
		 */
		public function GenteGameReplayController(gameId:int, board:GenteBoard, playersViewer:GentePlayersViewer, moveViewer:GenteMoveViewer, animateLayer:UIComponent){
			super();
			
			//set private references
            _gameId = gameId;
			_board = board;
			_playersViewer = playersViewer;
			_moveViewer = moveViewer;
			_animateLayer = animateLayer;
			_moves = new ArrayCollection();
			
			//initialize game service remote object
			_gameService = new RemoteObject();
			_gameService.destination = GAME_SERVICE_DESTINATION_NAME;
			_gameService.addEventListener(ResultEvent.RESULT, gameServiceResultHandler);
			_gameService.addEventListener(FaultEvent.FAULT, gameServiceFaultHandler);
			
			//initialize the move viewer
			_moveViewer.addEventListener(GenteMoveViewer.MOVE_EVENT_GOTO_MOVE, gotoMoveHandler);
			_moveViewer.board = _board;
			
			//get the game
			var call:Object = _gameService.getGame(_gameId);
			call.operation = GAME_SERVICE_GET_GAME_OP;
		}
		
		
		/*
		 * Game service result handler. Depending on the type of call 
		 * different actions are taken.
		 */
		private function gameServiceResultHandler(event:ResultEvent):void{
			var call:Object = event.token;
			switch (call.operation){
				case GAME_SERVICE_GET_GAME_OP:
					_game = GenteGame(event.result);
					var callPlayers:Object = _gameService.getPlayers(_gameId);
					callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;
					break;
				case GAME_SERVICE_GET_PLAYERS_OP:
					var pentePlayer:GentePlayer = GentePlayer(ArrayCollection(event.result)[0]);
					_playersViewer.players = ArrayCollection(event.result);
					var callMoves:Object = _gameService.getMoves(_gameId);
					callMoves.operation = GAME_SERVICE_GET_MOVES_OP;
					break;
				case GAME_SERVICE_GET_MOVES_OP:
					_moves = ArrayCollection(event.result)
					_moveViewer.initFromMoves(_moves);
					_selectedMoveInd = -1;
					gotoMove(GenteMove(_moves[0]));
					break;
			}
		}
		
		/*
		 * Handles faults from game service calls 
		 */
		private function gameServiceFaultHandler(event:FaultEvent):void{
			var errorMessage:ErrorMessage = ErrorMessage(event.message);
			Alert.show(event.fault.faultString, "Server Error");
		}
		
		private function gotoMoveHandler(event:DynamicEvent):void{
			
			var move:GenteMove = GenteMove(event.move);
			gotoMove(move);
		}
		
		private function gotoMove(move:GenteMove):void{

			if(_selectedMoveInd > -1 && move.orderId < GenteMove(_moves[_selectedMoveInd]).orderId){
				do{
					undoMove(GenteMove(_moves[_selectedMoveInd]));
					_selectedMoveInd --;					
				} while (move.orderId < GenteMove(_moves[_selectedMoveInd]).orderId && _selectedMoveInd > -1);
			}else if ((_selectedMoveInd < 0 || move.orderId > GenteMove(_moves[_selectedMoveInd]).orderId) && _selectedMoveInd < _moves.length){
				do{
					doMove(GenteMove(_moves[_selectedMoveInd + 1]));
					_selectedMoveInd ++;
				} while(move.orderId > GenteMove(_moves[_selectedMoveInd]).orderId && _selectedMoveInd < _moves.length);
			}
		}
		
		public function gotoFirstMove():void{
			gotoMove(GenteMove(_moves[0]));
		}
		
		public function gotoPreviousMove():void{
			if (_selectedMoveInd > 0){
				gotoMove(GenteMove(_moves[_selectedMoveInd - 1]));
			}
		}
		
		public function gotoNextMove():void{
			if (_selectedMoveInd < _moves.length - 2){
				gotoMove(GenteMove(_moves[_selectedMoveInd + 1]));
			}
		}
		
		public function gotoLastMove():void{
			gotoMove(GenteMove(_moves[_moves.length - 1]));
		}
		
		/*
		 * Animates a move
		 */
		private function doMove(move:Move):void{
			
			//define origin
			var startPoint:Point;
			var startSize:Number;
			var playerBtn:Button = _playersViewer.getPlayerButton(GentePlayer(move.player));
			startPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y + Color.getCellIconSize() / 2 + 5);
			startPoint = _playersViewer.localToGlobal(startPoint);
			startPoint = _animateLayer.globalToLocal(startPoint);
			startSize = Color.getCellIconSize();
						
			//define destination
			var boardCell:BoardCell = _board.getBoardCell(move.destinationCell.column, move.destinationCell.row);
			var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
			var endSize:Number = _board.tokenSize;
			endPoint = boardCell.localToGlobal(endPoint);
			endPoint = _animateLayer.globalToLocal(endPoint);
			
			//create new token
			var token:Token = new Token();
			token.cell = move.destinationCell;
			token.width = endSize;
			token.height = endSize;
			_animateLayer.addChild(token);
						
			//define motion animation
			var apX:AnimateProperty = new AnimateProperty(token);
			apX.fromValue = startPoint.x;
			apX.toValue = endPoint.x;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(token);
			apY.fromValue = startPoint.y;
			apY.toValue = endPoint.y;
			apY.duration = 1000;
			apY.property = "y";
			apY.addEventListener(EffectEvent.EFFECT_END, endDoMove);
			
			//define size animation
			var apXScale:AnimateProperty = new AnimateProperty(token);
			apXScale.property = "scaleX";
			apXScale.fromValue = startSize / endSize;
			apXScale.toValue = 1;
			apXScale.duration = 1000;
			var apYScale:AnimateProperty = new AnimateProperty(token);
			apYScale.property = "scaleY";
			apYScale.fromValue = startSize / endSize;
			apYScale.toValue = 1;
			apYScale.duration = 1000;
			
			//start effect
			apX.play();
			apY.play();
			apXScale.play();
			apYScale.play();
		}
		
		private function endDoMove(event:EffectEvent):void{
			
			var token:Token = Token(AnimateProperty(event.currentTarget).target);
			var boardCell:BoardCell = _board.getBoardCell(token.cell.column, token.cell.row);
			var move:GenteMove = GenteMove(_moves[_selectedMoveInd]);
			_animateLayer.removeChild(token);
			boardCell.token = token;
			_moveViewer.selectedMove = move;
			_playersViewer.setTurn(GentePlayer(move.player));
		}
		
		private function undoMove(move:Move):void{
			
			//define origin
			var boardCell:BoardCell = _board.getBoardCell(move.destinationCell.column, move.destinationCell.row);
			var startPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
			var startSize:Number = _board.tokenSize;
			startPoint = boardCell.localToGlobal(startPoint);
			startPoint = _animateLayer.globalToLocal(startPoint);
			
			//define destination
			var endPoint:Point;
			var endSize:Number;
			var playerBtn:Button = _playersViewer.getPlayerButton(GentePlayer(move.player));
			endPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y + Color.getCellIconSize() / 2 + 5);
			endPoint = _playersViewer.localToGlobal(endPoint);
			endPoint = _animateLayer.globalToLocal(endPoint);
			endSize = Color.getCellIconSize();
						
			//create new token
			var token:Token = new Token();
			token.cell = move.destinationCell;
			token.width = endSize;
			token.height = endSize;
			boardCell.token = null;
			_animateLayer.addChild(token);

			//define motion animation
			var apX:AnimateProperty = new AnimateProperty(token);
			apX.fromValue = startPoint.x;
			apX.toValue = endPoint.x;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(token);
			apY.fromValue = startPoint.y;
			apY.toValue = endPoint.y;
			apY.duration = 1000;
			apY.property = "y";
			apY.addEventListener(EffectEvent.EFFECT_END, endUndoMove);
			
			//define size animation
			var apXScale:AnimateProperty = new AnimateProperty(token);
			apXScale.property = "scaleX";
			apXScale.fromValue = startSize / endSize;
			apXScale.toValue = 1;
			apXScale.duration = 1000;
			var apYScale:AnimateProperty = new AnimateProperty(token);
			apYScale.property = "scaleY";
			apYScale.fromValue = startSize / endSize;
			apYScale.toValue = 1;
			apYScale.duration = 1000;
			
			//start effect
			apX.play();
			apY.play();
			apXScale.play();
			apYScale.play();
		}
		
		private function endUndoMove(event:EffectEvent):void{
			
			var token:Token = Token(AnimateProperty(event.currentTarget).target);
			var move:GenteMove = GenteMove(_moves[_selectedMoveInd]);
			_animateLayer.removeChild(token);
			_moveViewer.selectedMove = move;
			_playersViewer.setTurn(GentePlayer(move.player));
		}
	}
}
