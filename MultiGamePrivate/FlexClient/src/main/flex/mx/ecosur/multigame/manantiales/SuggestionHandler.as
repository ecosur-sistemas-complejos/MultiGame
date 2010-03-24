//copyright

package mx.ecosur.multigame.manantiales
{
    import flash.display.DisplayObject;
    import flash.events.MouseEvent;
    import flash.geom.Point;
    
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;
    import mx.ecosur.multigame.manantiales.enum.SuggestionStatus;
    import mx.ecosur.multigame.manantiales.token.*;
    import mx.effects.AnimateProperty;
    import mx.events.DynamicEvent;
    import mx.events.EffectEvent;
    import mx.managers.DragManager;
import mx.managers.PopUpManager;

public class SuggestionHandler {

        private var _controller:ManantialesGameController;

        private var _player:ManantialesPlayer;

        private var _isMoving:Boolean;
        
        private var _suggestionAlert:SuggestionAlert;

        private static const GAME_SERVICE_DO_SUGGESTION_OP:String = "makeSuggestion";

        public function SuggestionHandler (controller:ManantialesGameController) {
            _controller = controller;
            _player = controller._currentPlayer;
        }


        public function addSuggestion (suggestion:Suggestion):void {
            var move:ManantialesMove = suggestion.move;

            if (move.player.color == _player.color) {
                var boardCell:RoundCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                    move.destinationCell.column, move.destinationCell.row));

                /* Remove current */
                var current:Ficha = Ficha (move.currentCell);
                var boardCurrent:RoundCell = RoundCell (_controller._gameWindow.board.getBoardCell(
                    current.column, current.row));
                boardCurrent.token = new UndevelopedToken ();
                boardCurrent.token.cell = current;
                boardCurrent.reset();
                
                /* Animate in suggestion */

                //define origin
                var startPoint:Point;
                var startSize:Number;

                startPoint = new Point(current.width, current.height);
                startPoint = current.localToGlobal(startPoint);
                startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = _controller._gameWindow.board.tokenSize;

                //define destination
                var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
                var endSize:Number = _controller._gameWindow.board.tokenSize;
                endPoint = boardCell.localToGlobal(endPoint);
                endPoint = _controller._gameWindow.animateLayer.globalToLocal(endPoint);

                //create new token
                var destination:Ficha = Ficha (move.destinationCell);
                var token:ManantialesToken = new SuggestionToken(suggestion);
                
                token.cell = destination;
                token.width = endSize;
                token.height = endSize;
                _controller._gameWindow.animateLayer.addChild(token);

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
                
                token.blink(0);
                
                if (_suggestionAlert == null) {
                    _suggestionAlert = new SuggestionAlert();
                    _suggestionAlert.suggestionViewer = new SuggestionViewer();
                    _suggestionAlert.suggestionViewer.board = _controller._gameWindow.board;
                    _suggestionAlert.suggestionViewer.handler = this;
                    PopUpManager.addPopUp(_suggestionAlert, _controller._gameWindow, true);
                        PopUpManager.centerPopUp(_suggestionAlert);
                }
                
                _suggestionAlert.addSuggestion(suggestion);
            }
        }

        public function removeSuggestion (suggestion:Suggestion):void {
            var ficha:Ficha = Ficha (suggestion.move.destinationCell);
            
            //remove suggestion token
            for (var i:int = 0; i < _controller._gameWindow.animateLayer.numChildren; i++) {
                var obj:DisplayObject = _controller._gameWindow.animateLayer.getChildAt(i);
                if (obj is SuggestionToken) {
                    var suggestionToken:SuggestionToken = SuggestionToken (obj);
                    if (suggestionToken.cell.column == ficha.column && suggestionToken.cell.row == ficha.row) {
                        _controller._gameWindow.animateLayer.removeChild(obj);
                        break;
                    }
                }
            }
            
        }

        public function endRemoveSuggestion (event:EffectEvent):void {
            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(token.cell.column, token.cell.row);
            boardCell.token = token;
        }

        public function accept(event:DynamicEvent):void {
            var suggestion:Suggestion = Suggestion (event.data);
            if (_suggestionAlert != null) 
                _suggestionAlert.suggestionViewer.removeSuggestion(suggestion);
            suggestion.status = SuggestionStatus.ACCEPT;
            var call:Object = _controller._gameService.makeSuggestion (this._controller._game, suggestion);
            call.operation = "makeSuggestion";
        }

        public function reject(event:DynamicEvent):void {
            var suggestion:Suggestion = Suggestion (event.data);
            if (_suggestionAlert != null) {
                _suggestionAlert.suggestionViewer.removeSuggestion(suggestion);
                if (_suggestionAlert.suggestionViewer.length == 0) {
                    PopUpManager.removePopUp(_suggestionAlert);
                    _suggestionAlert = null;
                }           
            }
            var call:Object = _controller._gameService.makeSuggestion (this._controller._game, suggestion);
            call.operation = "makeSuggestion";
        }

        /* Drag/drop handlers for making suggestions on other player's boards */
        public function startSuggestion(evt:MouseEvent):void
        {
            if (_controller._currentPlayer.turn && _controller._currentPlayer.color == _player.color) {
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");
                
                /* Set the previous token to Undeveloped */
                var previous:ManantialesToken = ManantialesToken(evt.currentTarget);
                var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(previous.cell.column, previous.cell.row);
                var undeveloped:UndevelopedToken = new UndevelopedToken();
                undeveloped.cell = previous.cell;
                boardCell.token = undeveloped;
                boardCell.reset();
                
                // Add previous to the drag source 
                ds.addData(previous,"source");
                
                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
            }
        }

        public function makeSuggestion (move:ManantialesMove):void {
            move.status = String (MoveStatus.UNVERIFIED);
            move.mode = _controller._game.mode;

            var suggestion:Suggestion = new Suggestion();
            suggestion.move = move;
            suggestion.suggestor = _player;
            suggestion.status = SuggestionStatus.UNEVALUATED;

            var call:Object = new Object();
            call = _controller._gameService.makeSuggestion(_controller._game, suggestion);
            call.operation = GAME_SERVICE_DO_SUGGESTION_OP;
        }
    }
}