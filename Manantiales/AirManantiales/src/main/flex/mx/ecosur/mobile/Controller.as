/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/5/12
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile {

import flash.events.Event;
import flash.events.MouseEvent;
import flash.events.TimerEvent;
import flash.events.TouchEvent;
import flash.events.TransformGestureEvent;
import flash.geom.Point;
import flash.utils.Timer;

import mx.core.FlexGlobals;
import mx.ecosur.mobile.views.GameView;
import mx.ecosur.multigame.entity.ChatMessage;
import mx.ecosur.multigame.entity.GameGrid;
import mx.ecosur.multigame.enum.Color;
import mx.ecosur.multigame.enum.ExceptionType;
import mx.ecosur.multigame.enum.GameEvent;
import mx.ecosur.multigame.enum.GameState;
import mx.ecosur.multigame.enum.MoveStatus;
import mx.ecosur.multigame.manantiales.RoundCell;
import mx.ecosur.multigame.manantiales.entity.CheckCondition;
import mx.ecosur.multigame.manantiales.entity.Ficha;
import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
import mx.ecosur.multigame.manantiales.entity.Suggestion;
import mx.ecosur.multigame.manantiales.enum.ManantialesEvent;
import mx.ecosur.multigame.manantiales.enum.Mode;
import mx.ecosur.multigame.manantiales.enum.TokenType;
import mx.ecosur.multigame.manantiales.token.ForestToken;
import mx.ecosur.multigame.manantiales.token.IntensiveToken;
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.token.ModerateToken;
import mx.ecosur.multigame.manantiales.token.SilvopastoralToken;
import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
import mx.ecosur.multigame.manantiales.token.ViveroToken;
import mx.ecosur.multigame.util.MessageReceiver;

import mx.effects.Sequence;
import mx.events.DynamicEvent;
import mx.messaging.messages.ErrorMessage;
import mx.messaging.messages.IMessage;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

import spark.effects.Animate;
import spark.effects.easing.Bounce;

import spark.effects.Move;
import spark.effects.Fade;
import spark.effects.SetAction;
import spark.effects.animation.MotionPath;
import spark.effects.animation.SimpleMotionPath;
import spark.effects.easing.Sine;

import mx.resources.IResourceManager;
import mx.resources.ResourceManager;

[ResourceBundle ("ManantialesAir")]
public class Controller {

    private var resourceManager:IResourceManager = ResourceManager.getInstance();

    private var _view:GameView;

    private var gameService:RemoteObject;

    private var _gameId:int;

    private var _executingMove:ManantialesMove;

    private var receiver:MessageReceiver;

    private var timer:Timer;

    private var current:Number;

    private var limit:Number;

    public function Controller(view:GameView) {
        super();
        this._view = view;
        gameService = view.gameService;
        limit = 45 * 60 * 1000;
        timer = new Timer(1000, 0);
        timer.addEventListener(TimerEvent.TIMER, updateTime);
        timer.start();
    }

    public function resultHandler(event:ResultEvent):void {
        if (event.result is ManantialesGame) {
            var game:ManantialesGame = ManantialesGame(event.result);
            _gameId = game.id;
            if (receiver == null) {
                receiver = new MessageReceiver("multigame-destination", _gameId, FlexGlobals.topLevelApplication.amfChannelSet);
                receiver.addEventListener(MessageReceiver.PROCESS_MESSAGE, messageResultHandler);
            }
            updateGame(ManantialesGame(event.result));
        }
    }

    public function faultHandler(event:FaultEvent):void {
        var errorMessage:ErrorMessage = ErrorMessage(event.message);
        _view.status.showMessage(errorMessage.faultDetail);
        _view.status.flashMessage();
        if (errorMessage.extendedData != null) {
            if (errorMessage.extendedData[ExceptionType.EXCEPTION_TYPE_KEY] == ExceptionType.INVALID_MOVE) {
                undoMove(_executingMove);
            }
        }
    }

    public function messageResultHandler(event:DynamicEvent):void {
        var message:IMessage = event.message;
        var gameEvent:String = message.headers.GAME_EVENT;

        /* Holder objects */
        var game:ManantialesGame, checkCondition:CheckCondition, suggestion:Suggestion, move:ManantialesMove;

        switch (gameEvent) {
            case ManantialesEvent.BEGIN:
                game = ManantialesGame(message.body);
                updateGame(game);
                break;
            case ManantialesEvent.CHAT:
                updateChat(ChatMessage(message.body));
                break;
            case ManantialesEvent.END:
                game = ManantialesGame(message.body);
                end(game);
                break;
            case ManantialesEvent.MOVE_COMPLETE:
                move = ManantialesMove(message.body);
                addMove(move);
                break;
            case ManantialesEvent.PLAYER_CHANGE:
                game = ManantialesGame(message.body);
                updateGame(game);
                break;
            case ManantialesEvent.CONDITION_RAISED:
                checkCondition = CheckCondition(message.body);
                _view.alert(resourceManager.getString("ManantialesAir","checkcondition.raised") + " [" + checkCondition.reason + "]");
                break;
            case ManantialesEvent.CONDITION_RESOLVED:
                checkCondition = CheckCondition(message.body);
                _view.alert(resourceManager.getString("ManantialesAir","checkcondition.resolved")+ "[" + checkCondition.reason + "]");
                break;
            case ManantialesEvent.CONDITION_TRIGGERED:
                checkCondition = CheckCondition(message.body);
                _view.alert(resourceManager.getString("ManantialesAir","checkcondition.triggered"));
                refreshGame();
                break;
            case ManantialesEvent.SUGGESTION_EVALUATED:
                suggestion = Suggestion(message.body);
//                    _suggestionHandler.addSuggestion (suggestion);
                break;
            case ManantialesEvent.SUGGESTION_APPLIED:
                suggestion = Suggestion(message.body);
//                    _suggestionHandler.removeSuggestion (suggestion);
                break;
            case GameEvent.EXPIRED:
                move = ManantialesMove(message.body);
                end(_view.game, resourceManager.getString("ManantialesAir","move.expired"));
                break;
            default:
                break;
        }

    }

    public function updateGame(game:ManantialesGame):void {
        _view.game = game;
        this.current = _view.game.elapsedTime;

        for (var i:int = 0; i < _view.game.players.length; i++) {
            var p:ManantialesPlayer = ManantialesPlayer(_view.game.players [ i ]);
            if (p.name == FlexGlobals.topLevelApplication.registrant.name) {
                _view.player = p;
                break;
            }
        }

        _view.currentState = _view.game.mode;

        if (_view.game.state == GameState.PLAY) {
            var current:ManantialesPlayer = null;
            for (var i:int = 0; i < _view.game.players.length; i++) {
                var p:ManantialesPlayer = ManantialesPlayer(_view.game.players [ i ]);
                if (p.turn == true) {
                    current = p;
                    break;
                }
            }

            if (p.name == FlexGlobals.topLevelApplication.registrant.name) {
                _view.status.color = Color.getColorCode(p.color);
                _view.status.showMessage(resourceManager.getString("ManantialesAir","your.turn"));
            }
        } else {
            _view.status.showMessage(resourceManager.getString("ManantialesAir","waiing"));
        }

        populateBoard(_view.game);
        updatePlayers(_view.game);
        updateTime();
    }

    public function populateBoard(game:ManantialesGame):void {
        var grid:GameGrid = game.grid;
        _view.board.clearTokens();

        /* Setup undeveloped tokens first */
        for (var col:int = 0; col < _view.board.nCols; col++) {
            for (var row:int = 0; row < _view.board.nRows; row++) {
                var roundCell:RoundCell = RoundCell(_view.board.getBoardCell(col, row));
                if (roundCell != null) {
                    var tok:UndevelopedToken = new UndevelopedToken();
                    tok.col = col;
                    tok.row = row;
                    roundCell.token = tok;
                    decorate(tok);
                }
            }
        }

        var ficha:Ficha;
        var token:ManantialesToken;

        /* TODO: Move to store objects themselves. */
        if (grid && grid.cells && grid.cells.length > 0) {
            for (var i:int = 0; i < grid.cells.length; i++) {
                ficha = Ficha(grid.cells[i]);
                var token:ManantialesToken = createToken(ficha.type);
                token.ficha = ficha;
                _view.board.addToken(token);
                decorate(token);
            }
        }
    }

    public function updateChat(msg:ChatMessage):void {
        trace("Adding chatmessage: " + msg);

        _view.chat.addMessage(msg);
    }

    public function updatePlayers(game:ManantialesGame):void {
        _view.scorebox.game = game;
        _view.scorebox.updatePlayers();
        _executingMove = null;

        if (game.state != GameState.ENDED) {
            var p:ManantialesPlayer;

            for (var i:int = 0; i < game.players.length; i++) {
                var t:ManantialesPlayer = ManantialesPlayer(game.players [ i ]);
                /* One player will always have the turn */
                if (t.turn) {
                    p = t;
                    break;
                }
            }

            /* If active player is not current registrant, message the status box */
            if (p.name != FlexGlobals.topLevelApplication.registrant.name) {
                _view.status.color = Color.getColorCode(p.color);
                _view.status.showMessage(p.name + resourceManager.getString("ManantialesAir","player.turn"));
            } else {
                _view.status.color = Color.getColorCode(p.color);
                _view.status.showMessage(resourceManager.getString("ManantialesAir","your.turn"));
            }
        }
    }

    /* Adds a move to the board that has arrived over the wire */
    public function addMove(move:ManantialesMove):void {
        if (move.player.name != FlexGlobals.topLevelApplication.registrant.name) {
            _view.status.showMessage(move.player.name + " has moved.");
            var target:Ficha = Ficha(move.destinationCell);

            /* NOTE: Need to add in handling of replacement moves */

            var rc:RoundCell = RoundCell(_view.board.getBoardCell(target.column, target.row));
            var dest:Point = new Point();
            dest.x = rc.x + (rc.width / 2);
            dest.y = rc.y + (rc.height / 2);

            var current:ManantialesToken = ManantialesToken(rc.token);
            var next:ManantialesToken = createToken(move.type);
            next.ficha = target;
            animateMessagedMove(current, next,  rc,  dest);
        }
    }

    public function undoMove(move:ManantialesMove):void {
        _view.status.showMessage("Undoing move: " + move);
        var currentToken:ManantialesToken, replacedToken:ManantialesToken;
        var rc:RoundCell = RoundCell(_view.board.getBoardCell(move.destinationCell.column, move.destinationCell.row));

        /* If there is a 'replacementType', the moved replaced a token with a known value */
        if (move.replacementType != null) {
            replacedToken = createToken(move.replacementType);
            var f:Ficha = new Ficha();
            f.column = move.currentCell.column;
            f.row = move.currentCell.row;
            f.color = move.currentCell.color;
            replacedToken.ficha = f;
        } else {
            /* otherwise, this move replaced a UNDEVELOPED token */
            replacedToken = createToken(TokenType.UNDEVELOPED);
            if (rc.row == 4 || rc.column == 4) {
                /* Reset border cells to neutral color */
                rc.setStyle("cellBgColor", 0xA0A0A0);
            }
        }

        if (move.replacementType != TokenType.INTENSIVE || move.replacementType == null) {
            replacedToken.addEventListener(TouchEvent.TOUCH_TAP, tapHandler);
            replacedToken.addEventListener(MouseEvent.CLICK, tapHandler);
        }

        var dest:Point = new Point();
        dest.x = rc.x + (rc.width / 2);
        dest.y = rc.y + (rc.height / 2);

        currentToken = ManantialesToken(rc.token);
        animateMove(currentToken, replacedToken, rc, dest);
        _executingMove = null;
    }

    public function quitGame():void {
        gameService.quitGame(_view.game, _view.player);
        _view.navigator.popView();
    }

    public function zoomHandler(event:TransformGestureEvent):void {
        _view.board.scaleX *= event.scaleX;
        _view.board.scaleY *= event.scaleY;
    }

    public function panHandler(event:TransformGestureEvent):void {
        _view.board.x += event.offsetX;
        _view.board.y += event.offsetY;
    }    internal function tapHandler(event:Event):void {
        var dest:Point = new Point();
        var targ:ManantialesToken = ManantialesToken(event.currentTarget);
        var type:Object = _view.tokenTypes.getItemAt(_view.tokenType.selectedIndex);

        var column:int, row:int;
        if (targ is UndevelopedToken) {
            column = UndevelopedToken(targ).col;
            row = UndevelopedToken(targ).row;
        } else {
            column = targ.ficha.column;
            row = targ.ficha.row;
        }

        var rc:RoundCell = RoundCell(_view.board.getBoardCell(column, row));
        dest.x = rc.x + (rc.width / 2);
        dest.y = rc.y + (rc.height / 2);

        var tok:ManantialesToken = createToken(String(type.data));
        var ficha:Ficha = new Ficha();
        ficha.type = tok.type;
        ficha.row = rc.row;
        ficha.column = rc.column;
        ficha.color = _view.player.color;
        tok.ficha = ficha;

        /* Send the move over the wire */
        if (validateMove(rc, tok)) {
            rc.select(tok.cell.colorCode);
            decorate(tok);
            animateMove(targ, tok, rc, dest);
            var move:ManantialesMove = new ManantialesMove();
            move.status = MoveStatus.UNVERIFIED;
            move.mode = _view.game.mode;
            move.player = _view.player;
            move.currentCell = targ.ficha;
            move.destinationCell = tok.ficha;
            sendMove(move);
        }
    }

    protected function updateTime(event:TimerEvent = null):void {
        if (current < limit) {
            current += 1000;
            _view.timer.showMessage(currentTime() + " / 45:00");
        } else {
            _view.status.showMessage(resourceManager.getString("ManantialesAir","game.expired"))
            _view.timer.showMessage("45:00 / 45:00");
            _view.status.flashMessage();
            _view.timer.flashMessage();
            timer.stop();
            end(_view.game, "Time expired.")
        }
    }

    internal function end(game:ManantialesGame, msg:String = null):void {
        if (game != null && game.state == GameState.ENDED) {
            if (msg == null) {
                _view.alert("Game over.");
            } else {
                _view.alert("Game over. " + msg);
            }
        }
    }

    private function currentTime():String {
        var ret:String;
        var minutes:int, seconds:Number;
        seconds = Math.floor(current / 1000);
        if (seconds > 59) {
            minutes = Math.floor(seconds / 60);
            if (minutes >= 45)
                ret = "45:00";
            else if (seconds % 60 > 9)
                ret = String(minutes) + ":" + String(seconds % 60);
            else
                ret = String(minutes) + ":0" + String(seconds % 60);
        } else {
            if (seconds > 9)
                ret = "0:" + String(Math.floor(seconds));
            else
                ret = "0:0" + String(Math.floor(seconds));
        }

        return ret;
    }

    private function refreshGame():void {
        gameService.getGame(_view.game.id);
    }

    protected function animateMessagedMove(start:ManantialesToken, end:ManantialesToken, roundCell:RoundCell, dest:Point) {
        animateMove(start, end, roundCell,  dest);
    }

    protected function animateMove(start:ManantialesToken, end:ManantialesToken, roundCell:RoundCell, dest:Point):void {

        /* Sin easer */
        var sine:Sine = new Sine();
        sine.easeInFraction = 0.7;

        /* Move effect */
        var drop:Move = new Move();
        drop.xFrom = start.x;
        drop.yFrom = start.y;
        drop.xTo = start.x;
        drop.yTo = _view.height - start.y;

        drop.target = start;
        drop.easer = sine;
        drop.duration = 350;

        /* Fade effect on existing token (after drop) */
        var fade:Fade = new Fade();
        fade.target = start;
        fade.alphaFrom = 1.0;
        fade.alphaTo = 0;
        fade.duration = 800;

        /* Set action */
        var sa:SetAction = new SetAction();
        sa.target = roundCell;
        sa.property = "token";
        sa.value = end;

        /* Bounce effect on new token */
        var grw:Animate = new Animate();
        grw.easer = new Bounce();
        grw.target = end;
        var inc:SimpleMotionPath = new SimpleMotionPath();
        inc.property = "scaleX";
        inc.valueFrom = 1.0;
        inc.valueTo = 1.5;
        var dec:SimpleMotionPath = new SimpleMotionPath();
        dec.property = "scaleX";
        dec.valueFrom = 1.5;
        dec.valueTo = 1.0;
        var vect:Vector.<MotionPath> = new Vector.<MotionPath>();
        vect.push(inc);
        vect.push(dec);
        grw.motionPaths = vect;

        var seq:Sequence = new Sequence();
        seq.addChild(drop);
        seq.addChild(fade);
        seq.addChild(sa);
        seq.addChild(grw);
        seq.play();
    }

    private function createToken(type:String):ManantialesToken {
        var token:ManantialesToken;

        switch (type) {
            case TokenType.FOREST:
                token = new ForestToken();
                break;
            case TokenType.MODERATE:
                token = new ModerateToken();
                break;
            case TokenType.INTENSIVE:
                token = new IntensiveToken();
                break;
            case TokenType.VIVERO:
                token = new ViveroToken();
                break;
            case TokenType.SILVOPASTORAL:
                token = new SilvopastoralToken();
                break;
            case TokenType.UNDEVELOPED:
                token = new UndevelopedToken();
                break;
            default:
                break;
        }

        return token;

    }

    private function validateMove(boardCell:RoundCell, token:ManantialesToken):Boolean {
        var ret:Boolean = false;
        if (boardCell.color == token.cell.color && token.cell.color == _view.player.color) {
            ret = true;
        } else if (boardCell.color == Color.UNKNOWN &&
                (token.type == TokenType.UNDEVELOPED || token.cell.color == _view.player.color))
        {
            switch (token.cell.color) {
                case Color.YELLOW:
                    ret = (boardCell.column < 5 && boardCell.row < 5);
                    break;
                case Color.PURPLE:
                    ret = (boardCell.column < 5 && boardCell.row > 3);
                    break;
                case Color.RED:
                    ret = (boardCell.column > 3 && boardCell.row < 5);
                    break;
                case Color.BLACK:
                    ret = (boardCell.column > 3 && boardCell.row > 3);
                    break;
            }
        }
        /* It must be the current player's turn */
        ret = ret && _view.player.turn && _view.player.color == token.cell.color;
        /* There cannot be a move executing */
        ret = ret && _executingMove == null;
        return ret;
    }

    private function sendMove(move:ManantialesMove):void {
        _view.status.showMessage(resourceManager.getString("ManantialesAir","move.processing"));
        this._executingMove = move;
        gameService.doMove(_view.game, move);
    }

    private function decorate(tok:ManantialesToken):void {
        switch (_view.currentState) {
            case Mode.COMPETITIVE:
            // Fall through
            case Mode.SILVOPASTORAL:
                if (tok.type != TokenType.INTENSIVE) {
                    tok.addEventListener(MouseEvent.CLICK, tapHandler);
                }
                break;
            default:
                // All tokens are "hot" in puzzle modalities
                tok.addEventListener(MouseEvent.CLICK, tapHandler);
                break;
        }
    }
}
}
