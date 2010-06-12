package mx.ecosur.multigame.manantiales.token
{
    import flash.display.Shape;
    
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.component.TokenStore;
    import mx.ecosur.multigame.manantiales.ManantialesBoard;
    import mx.ecosur.multigame.manantiales.ManantialesGameController;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.events.DragEvent;
    import mx.managers.DragManager;

    public class ManantialesTokenStore extends TokenStore
    {
        protected static const INITIAL_N_TOKENS:int = 6;
        
        protected var _board:ManantialesBoard;
        protected var _controller:ManantialesGameController;
        protected var _tokenType:String;
        public var inited:Boolean;
        
        public function set controller (controller:ManantialesGameController):void {
            _controller = controller;
        }
        
        public function set board (board:ManantialesBoard):void {
           _board = board;
        }
        
        public function get tokenType():String {
        	return _tokenType;
        }

        public function reset():void {
            this.addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
            this.addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
        }
        
        protected function dragEnterHandler(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){
                var token:ManantialesToken = ManantialesToken(evt.dragSource.dataForFormat("token"));
                
                if(token.type == this._tokenType && token.cell.color == _currentPlayer.color){
                	DragManager.acceptDragDrop(this);
                }
            }
        }
        
		protected function dragDropHandler(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){
                var token:ManantialesToken = ManantialesToken(evt.dragSource.dataForFormat("token"));
                
                if(token.type == this._tokenType && token.cell.color == _currentPlayer.color){
                	
                	/* Check that move is from the board and not the token store */
                	if(token.cell && token.cell.column >= 0 && token.cell.row >= 0){
                		var boardCell:BoardCell = _board.getBoardCell(token.cell.column, token.cell.row);
                		boardCell.token = new UndevelopedToken ();
                		addToken();
                		
                		var move:ManantialesMove = new ManantialesMove ();
                		move.currentCell = token.ficha;
                		move.player = _controller._currentPlayer;
                		move.mode = _controller._game.mode;
                		var call:Object;
                        call = _controller._gameService.doMove(_controller._game, move);
                        call.operation = "doMove";
                	}
                }
            }
        }

        override protected function createChildren():void{

            // Create background
            _bg = new Shape();
            addChild(_bg);

            // Create initial tokens
            _nTokens = 0;
            for (var i:int = 0; i < INITIAL_N_TOKENS; i++){
                addToken();
            }
        }
    }
}