//copyright

package mx.ecosur.multigame.manantiales
{
    import flash.geom.Point;
    import flash.events.MouseEvent;    

    import mx.controls.Alert;
    import mx.controls.Button;
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;
    import mx.ecosur.multigame.manantiales.enum.SuggestionStatus;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.ecosur.multigame.manantiales.token.IntensiveToken;
    import mx.ecosur.multigame.manantiales.token.ManantialesToken;
    import mx.ecosur.multigame.manantiales.token.SuggestionToken;
    import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
    import mx.effects.AnimateProperty;
    import mx.events.DragEvent;
    import mx.managers.DragManager;
    import mx.managers.PopUpManager;

    public class SuggestionHandler {

        private var _controller:ManantialesGameController;

        private var _player:ManantialesPlayer;

        public var _isMoving:Boolean;

        private static const GAME_SERVICE_DO_SUGGESTION_OP:String = "makeSuggestion";

        private var _alert:SuggestionAlert;

        public function SuggestionHandler (controller:ManantialesGameController) {
            _controller = controller;
            _player = controller._currentPlayer;
        }


        public function addSuggestion (suggestion:Suggestion):void {
            var move:ManantialesMove = suggestion.move;

            var boardCell:RoundCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                move.destinationCell.column, move.destinationCell.row));
            if (!boardCell.token is UndevelopedToken ||
                boardCell.token is IntensiveToken)
            {
                _controller._gameWindow.moveViewer.selectedMove = move;
                return;
            }

            var destination:Ficha = Ficha(move.destinationCell);

            //define origin
            var startPoint:Point;
            var startSize:Number;

            if(move.player.id == _controller._currentPlayer.id && _controller._currentPlayer.turn){
                switch (destination.type) {
                    case TokenType.FOREST:
                       startPoint = new Point(_controller._gameWindow.forestStore.width, _controller._gameWindow.forestStore.height);
                       startPoint = _controller._gameWindow.forestStore.localToGlobal(startPoint);
                       break;
                    case TokenType.INTENSIVE:
                        startPoint = new Point(_controller._gameWindow.intensiveStore.width, _controller._gameWindow.intensiveStore.height);
                        startPoint = _controller._gameWindow.intensiveStore.localToGlobal(startPoint);
                        break;
                    case TokenType.MODERATE:
                        startPoint = new Point(_controller._gameWindow.moderateStore.width, _controller._gameWindow.moderateStore.height);
                        startPoint = _controller._gameWindow.moderateStore.localToGlobal(startPoint);
                        break;
                    case TokenType.SILVOPASTORAL:
                        startPoint = new Point(_controller._gameWindow.silvoStore.width, _controller._gameWindow.silvoStore.height);
                        startPoint = _controller._gameWindow.silvoStore.localToGlobal(startPoint);
                        break;
                    case TokenType.VIVERO:
                        startPoint = new Point(_controller._gameWindow.viveroStore.width, _controller._gameWindow.viveroStore.height);
                        startPoint = _controller._gameWindow.viveroStore.localToGlobal(startPoint);
                        break;
                   default:
                        break;
                }

                startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = _controller._gameWindow.board.tokenSize;

            } else{
                var playerBtn:Button = _controller._gameWindow.playersViewer.getPlayerButton(
                    ManantialesPlayer(move.player));
                startPoint = new Point(
                    playerBtn.x + Color.getCellIconSize() / 2 + 5,
                    playerBtn.y + Color.getCellIconSize() / 2 + 5);
                startPoint = _controller._gameWindow.playersViewer.localToGlobal(startPoint);
                startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = Color.getCellIconSize();
            }

            //define destination
            var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var endSize:Number = _controller._gameWindow.board.tokenSize;
            endPoint = boardCell.localToGlobal(endPoint);
            endPoint = _controller._gameWindow.animateLayer.globalToLocal(endPoint);

            //create new token
            var token:SuggestionToken = new SuggestionToken (suggestion);

            token.cell = move.destinationCell;
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

            /* Pop up an Accept/Reject dialogue to determine what to do with suggestion */
            
            if (_alert == null)
                _alert = new SuggestionAlert();
            _alert.suggestion = suggestion;

            PopUpManager.addPopUp(_alert, _controller._gameWindow, true);
                    PopUpManager.centerPopUp(_alert);            
        }

        public function accept(suggestion:Suggestion):void {
            Alert.show("Accepted! [" + suggestion.toString() + "]");

            /* Do move on back end */
            var call:Object = _controller._gameService.doMove(_controller._game, suggestion.move);
            call.operation = "doMove";
            
        }

        public function reject(suggestion:Suggestion):void {
            Alert.show ("Rejected! [" + suggestion.toString() + "]");
        }

        /* Start and end move handlers for Suggestion token stores */
        public function startMove(evt:MouseEvent):void{
                // initialize drag source
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");

                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
                _isMoving = true;
        }

        public function endMove(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){
                _isMoving = false;
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                token.selected = false;
            }
        }

        /* Drag/drop handlers for making suggestions on other player's boards */
        public function startSuggestion(evt:MouseEvent):void
        {
            Alert.show("Start suggestion.");

            var token:ManantialesToken = ManantialesToken(evt.currentTarget);
            var ds:DragSource = new DragSource();
            ds.addData(token, "token");

            // create proxy image and start drag
            var dragImage:IFlexDisplayObject = token.createDragImage();
            DragManager.doDrag(token, ds, evt, dragImage);
        }

        public function endSuggestion(evt:MouseEvent):void {
            var token:ManantialesToken = ManantialesToken(evt.target);
            var move:ManantialesMove = new ManantialesMove();
            move.currentCell = token.cell;
            move.destinationCell = ManantialesToken(evt.target).cell;

            var suggestion:Suggestion = new Suggestion();
            suggestion.move = move;
            suggestion.suggestor = this._controller._currentPlayer;
            suggestion.status = SuggestionStatus.UNEVALUATED;
            var call:Object = new Object();
            call = _controller._gameService.makeSuggestion(_controller._game, suggestion);
            call.operation = GAME_SERVICE_DO_SUGGESTION_OP;
        }
    }
}