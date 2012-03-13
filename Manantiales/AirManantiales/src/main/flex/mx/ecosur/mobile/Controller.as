/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/5/12
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile {

import mx.core.FlexGlobals;
import mx.ecosur.mobile.views.GameView;
import mx.ecosur.multigame.entity.GameGrid;
import mx.ecosur.multigame.enum.Color;
import mx.ecosur.multigame.enum.GameState;
import mx.ecosur.multigame.manantiales.ManantialesBoard;
import mx.ecosur.multigame.manantiales.RoundCell;
import mx.ecosur.multigame.manantiales.entity.Ficha;
import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
import mx.ecosur.multigame.manantiales.enum.TokenType;
import mx.ecosur.multigame.manantiales.token.ForestToken;
import mx.ecosur.multigame.manantiales.token.IntensiveToken;
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.token.ModerateToken;
import mx.ecosur.multigame.manantiales.token.SilvopastoralToken;
import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
import mx.ecosur.multigame.manantiales.token.ViveroToken;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

public class Controller {

        private var _view:GameView;
    
        public function Controller(view:GameView) {
            super();
            this._view = view;
        }

        public function resultHandler(event:ResultEvent):void {
            _view.game = ManantialesGame(event.result);
            for (var i:int = 0; i < _view.game.players.length; i++) {
                var p:ManantialesPlayer = ManantialesPlayer(_view.game.players [ i ]);
                if (p.name == FlexGlobals.topLevelApplication.registrant.name) {
                    _view.player = p;
                    break;
                }
            }
    
            _view.currentState = _view.game.mode;
    
            if (_view.game.state = GameState.PLAY) {
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
                    _view.status.showMessage("It's your turn");
                } else {
                    _view.status.color = Color.getColorCode(p.color);
                    _view.status.showMessage(p.name + " to move");
                }
    
            } else {
                _view.status.showMessage("Waiting for more players ...");
            }
    
            _view.timer.showMessage(_view.game.elapsedTime + " ms elapsed");
            populateBoard(_view.game);
            populateScores(_view.game);
    
        }
    
        public function faultHandler(event:FaultEvent):void {
            trace("Fault: " + event.fault);
    
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
                        roundCell.token = tok;
                    }
                }
            }

            var ficha:Ficha;
            var token:ManantialesToken;

            /* TODO: Move to store objects themselves. */
            if (grid && grid.cells && grid.cells.length > 0) {
                for (var i:int = 0; i < grid.cells.length; i++){
                    ficha = Ficha(grid.cells[i]);
                    switch (ficha.type) {
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
                        default:
                            break;
                    }
                    token.ficha = ficha;
                    _view.board.addToken(token);
                }
            }

        }

        public function populateScores(game:ManantialesGame):void {
            _view.scorebox.game = game;
           _view.scorebox.updatePlayers();
        }

    }
}
