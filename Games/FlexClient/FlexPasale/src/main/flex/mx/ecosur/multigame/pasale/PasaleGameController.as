package mx.ecosur.multigame.pasale {

import mx.collections.ArrayCollection;
import mx.controls.Alert;
import mx.ecosur.multigame.component.BoardCell;
import mx.ecosur.multigame.entity.ChatMessage;
import mx.ecosur.multigame.entity.GamePlayer;
import mx.ecosur.multigame.enum.Color;
import mx.ecosur.multigame.enum.ExceptionType;
import mx.ecosur.multigame.enum.GameEvent;
import mx.ecosur.multigame.entity.pasale.PasaleGame;
import mx.ecosur.multigame.enum.MoveStatus;
import mx.ecosur.multigame.pasale.entity.PasaleGrid;
import mx.ecosur.multigame.entity.pasale.PasaleMove;
import mx.ecosur.multigame.entity.pasale.PasalePlayer;
import mx.ecosur.multigame.util.MessageReceiver;
import mx.events.CloseEvent;
import mx.events.DynamicEvent;
import mx.messaging.messages.ErrorMessage;
import mx.messaging.messages.IMessage;
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

public class PasaleGameController {

        public var _gameWindow:PasaleGameWindow;
        public var _gameService:RemoteObject;

        public var _currentPlayer:PasalePlayer;
        public var _game:PasaleGame;

        public var _executingMove:PasaleMove;
        public var _ready:Boolean;


        private var _gameId:int;

        private var _msgReceiver:MessageReceiver;

        // constants
        private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
        private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
        private static const GAME_SERVICE_GET_GRID_OP:String = "getGameGrid";
        private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
        private static const GAME_SERVICE_DO_MOVE_OP:String = "doMove";

        /* Internationalization */
        private var resourceManager:IResourceManager = ResourceManager.getInstance();


        public function PasaleGameController (gameWindow:PasaleGameWindow)
        {
            // set references
            _gameWindow = gameWindow;
            _currentPlayer = PasalePlayer (gameWindow.currentPlayer);
            _game = PasaleGame(gameWindow.currentGame);
            _gameId = _game.id;

             // initialize game service remote object
            _gameService = new RemoteObject();
            _gameService.destination = GAME_SERVICE_DESTINATION_NAME;
            _gameService.addEventListener(ResultEvent.RESULT,
                gameServiceResultHandler);
            _gameService.addEventListener(FaultEvent.FAULT,
                gameServiceFaultHandler);

            // initialize message receiver
            _msgReceiver = new MessageReceiver(MESSAGING_DESTINATION_NAME, _game.id);
            _msgReceiver.addEventListener(MessageReceiver.PROCESS_MESSAGE, processMessage);

            var callGrid:Object = _gameService.getGameGrid(_gameId);
            callGrid.operation = GAME_SERVICE_GET_GRID_OP;
            _ready = true;
        }

        public function ready():Boolean {
            return _ready;
        }

        public function sendMove (move:PasaleMove):void {            
            var call:Object = _gameService.doMove(_game, move);
            call.operation = "doMove";
            _executingMove = move;
        }

        public function quitGame (gamePlayer:GamePlayer):void {
            var call:Object = _gameService.quitGame(_game, gamePlayer);
            call.operation = "quitGame";
        }    

        public function destroy():void {
            _msgReceiver.destroy();
        }

 /*
         * Process the different types of messages received by the MessageReciever,
         * reordered and dispatched. All messages contain a game event
         * header, based on this different actions are taken.
         */
        private function processMessage(event:DynamicEvent):void {
            var message:IMessage = event.message;
            var gameEvent:String = message.headers.GAME_EVENT;
            var move:PasaleMove = null;

            switch (gameEvent) {
                case GameEvent.MOVE_COMPLETE:
                    move= PasaleMove(message.body);
                    handleMove(move);
                    break;
                case GameEvent.CONDITION_TRIGGERED:
                    var me:MutationEvent = MutationEvent(message.body);
                    handleMutation(me);
                    break;
                case GameEvent.PLAYER_CHANGE:
                    var game:PasaleGame = PasaleGame(message.body);
                    updatePlayers(game.players);
                    var callGrid:Object = _gameService.getGameGrid(_gameId);
                    callGrid.operation = GAME_SERVICE_GET_GRID_OP;
                    _ready = false;
                    break;
                default:
                    Alert.show("Unknown Event! [" + gameEvent + "]");
            }
        }

        private function handleMutation(event:MutationEvent):void {
             _gameWindow.doMutation(event);
        }

        private function handleMove(move:PasaleMove):void {
            _ready = true;
            if (move.status = MoveStatus.EVALUATED)
                _gameWindow.doMove(move);
            else
                Alert.show("Unhandled Move. Status:  " + move.status);
        }

        /*
         * Game service result handler. Depending on the type of call
         * different actions are taken.
         */
        private function gameServiceResultHandler(event:ResultEvent):void {
            var call:Object = event.token;
            switch (call.operation){
                case GAME_SERVICE_GET_GRID_OP:
                    initGrid(PasaleGrid(event.result));
                    break;
                case GAME_SERVICE_GET_PLAYERS_OP:
                    var game:PasaleGame = PasaleGame(event.result);
                    updatePlayers(game.players);
                    break;
                case GAME_SERVICE_DO_MOVE_OP:
                    _executingMove = null;
                    break;
            }
        }

/*
         * Handles faults from game service calls
         */
        private function gameServiceFaultHandler(event:FaultEvent):void{
            var errorMessage:ErrorMessage = ErrorMessage(event.message);
            if (errorMessage.extendedData != null){
                if(errorMessage.extendedData[ExceptionType.EXCEPTION_TYPE_KEY] == ExceptionType.INVALID_MOVE){
                    var fnc:Function = function (event:CloseEvent):void{
                        invalidMove(_executingMove);
                    }
                    Alert.show(resourceManager.getString("Pasale","pasale.move.invalid"),
                        resourceManager.getString("Pasale","pasale.move.error.title"), Alert.OK, null,
                        fnc);
                }
            }else{
                Alert.show(event.fault.faultString, resourceManager.getString("Pasale",
                        "pasale.controller.server.error"));
            }
        }

        private function initGrid(grid:PasaleGrid):void {
            _gameWindow.board.grid = grid;
            _ready = true;
        }

        private function updatePlayers(players:ArrayCollection):void {
            _gameWindow.scoreboard.players = players;
            _gameWindow.scoreboard.draw();
        }

        private function invalidMove(move:PasaleMove):void {
            Alert.show("INVALID MOVE: " + move);
            
        }
    }
}
