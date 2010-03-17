//copyright

package mx.ecosur.multigame.manantiales
{
    import flash.geom.Point;
    import flash.events.MouseEvent;

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.controls.Button;
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;
    import mx.ecosur.multigame.manantiales.enum.SuggestionStatus;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.ecosur.multigame.manantiales.token.ForestToken;
    import mx.ecosur.multigame.manantiales.token.IntensiveToken;
    import mx.ecosur.multigame.manantiales.token.ManantialesToken;
    import mx.ecosur.multigame.manantiales.token.ModerateToken;
    import mx.ecosur.multigame.manantiales.token.SuggestionToken;
    import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
    import mx.ecosur.multigame.manantiales.token.ViveroToken;
    import mx.ecosur.multigame.tablon.token.SilvopastoralToken;
    import mx.effects.AnimateProperty;
    import mx.events.DragEvent;
    import mx.events.DynamicEvent;
    import mx.events.EffectEvent;
    import mx.managers.DragManager;
    import mx.managers.PopUpManager;

    public class SuggestionHandler {

        private var _controller:ManantialesGameController;

        private var _player:ManantialesPlayer;

        private var _isMoving:Boolean;

        private var _suggestions:ArrayCollection;

        private var _rejections:ArrayCollection;

        private static const GAME_SERVICE_DO_SUGGESTION_OP:String = "makeSuggestion";

        private var _alert:SuggestionAlert;

        public function SuggestionHandler (controller:ManantialesGameController) {
            _controller = controller;
            _player = controller._currentPlayer;
            _suggestions = new ArrayCollection();
            _rejections = new ArrayCollection();
        }


        public function addSuggestion (suggestion:Suggestion):void {
            if (_suggestions.contains(suggestion) || _rejections.contains(suggestion))
                return;
            _suggestions.addItem(suggestion);

            var move:ManantialesMove = suggestion.move;

            if (move.player.color == _player.color) {
                var boardCell:RoundCell = RoundCell(_controller._gameWindow.board.getBoardCell(
                    move.destinationCell.column, move.destinationCell.row));

                //var destination:Ficha = Ficha(move.destinationCell);
                var current:Ficha = new Ficha();
                current.row = move.currentCell.row;
                current.column = move.currentCell.column;
                current.color = move.currentCell.color;
                current.type = TokenType.UNDEVELOPED;                                

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

                token.blink();


                /* Pop up an Accept/Reject dialogue to determine what to do with suggestion */

                if (_alert == null)
                    _alert = new SuggestionAlert();
                _alert.suggestion = suggestion;
                _alert.addEventListener("accept", accept);
                _alert.addEventListener("reject", reject);

                PopUpManager.addPopUp(_alert, _controller._gameWindow, true);
                        PopUpManager.centerPopUp(_alert);
            }
        }

        public function removeSuggestion (suggestion:Suggestion):void {
            var boardCell:BoardCell;
            var column:int, row:int;

            if (suggestion.status == SuggestionStatus.ACCEPT) {
                column = suggestion.move.currentCell.column
                row = suggestion.move.currentCell.row;
            } else {
                column = suggestion.move.destinationCell.column
                row = suggestion.move.destinationCell.row;
            }

            boardCell = _controller._gameWindow.board.getBoardCell(column, row);

            var startPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var startSize:Number = _controller._gameWindow.board.tokenSize;
            startPoint = boardCell.localToGlobal(startPoint);
            startPoint = _controller._gameWindow.animateLayer.globalToLocal(startPoint);

            //define destination
            var playerBtn:Button = _controller._gameWindow.playersViewer.getPlayerButton(ManantialesPlayer(suggestion.suggestor));
            var endPoint:Point = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y +
                    Color.getCellIconSize() / 2 + 5);

            endPoint = _controller._gameWindow.playersViewer.localToGlobal(endPoint);
            endPoint = _controller._gameWindow.animateLayer.globalToLocal(endPoint);
            var endSize:Number = Color.getCellIconSize();

            //create new token
            var token:ManantialesToken = new ManantialesToken();
            token.cell = suggestion.move.destinationCell;
            token.width = endSize;
            token.height = endSize;
            token.visible = false;
            _controller._gameWindow.animateLayer.addChild(token);

            /*
            // restore previous token
            for (var i:int = 0; i < _controller._moves.length; i++) {
                if (_controller._moves.length > 0) {
                    var possible:ManantialesMove = _controller._moves [ _controller._moves.length - i];
                    if (possible.destinationCell == suggestion.move.currentCell) {
                        var ficha:Ficha = Ficha (possible.destinationCell);
                        switch (ficha.type) {
                            case TokenType.INTENSIVE:
                                boardCell.token = new IntensiveToken();
                                break;
                            case TokenType.MODERATE:
                                boardCell.token = new ModerateToken();
                                break;
                            case TokenType.FOREST:
                                boardCell.token = new ForestToken();
                                break;
                            case TokenType.VIVERO:
                                boardCell.token = new ViveroToken();
                                break;
                            case TokenType.SILVOPASTORAL:
                                boardCell.token = new SilvopastoralToken();
                                break;
                            default:
                                boardCell.token = new UndevelopedToken(ficha.column, ficha.row);

                        }
                    }
                }
            } */
            
            //boardCell.token = new UndevelopedToken (column, row);
            boardCell.token = null;
            boardCell.reset();

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
            apY.addEventListener(EffectEvent.EFFECT_END, endRemoveSuggestion);

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

            // make the animation token visible
            token.visible = true;

            //start effect
            apX.play();
            apY.play();
            apXScale.play();
            apYScale.play();
        }

        public function endRemoveSuggestion (event:EffectEvent) {
            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            var boardCell:BoardCell = _controller._gameWindow.board.getBoardCell(token.cell.column, token.cell.row);
                _controller._gameWindow.animateLayer.removeChild(token);

            boardCell.token = token;
            //boardCell.token.blink(1);
        }

        public function accept(event:DynamicEvent):void {
            var suggestion:Suggestion = Suggestion (event.data);
            suggestion.status = SuggestionStatus.ACCEPT;            
            var call:Object = _controller._gameService.makeSuggestion (this._controller._game, suggestion);
            call.operation = "makeSuggestion";
            PopUpManager.removePopUp(_alert);
        }

        public function reject(event:DynamicEvent):void {
            var suggestion:Suggestion = Suggestion (event.data);
            var call:Object = _controller._gameService.makeSuggestion (this._controller._game, suggestion);
            call.operation = "makeSuggestion";
            PopUpManager.removePopUp(_alert);
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
            if (_controller._currentPlayer.turn && _controller._currentPlayer.color == _player.color) {
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");

                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
            }
        }

        public function makeSuggestion (move:ManantialesMove):void {          
            move.status = String (MoveStatus.UNVERIFIED);
            move.mode = _controller._game.mode;
            move.id = 0;

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