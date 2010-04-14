package mx.ecosur.multigame.manantiales.token
{
    import flash.display.Shape;
    
    import mx.ecosur.multigame.component.TokenStore;
    import mx.ecosur.multigame.manantiales.ManantialesBoard;

    public class ManantialesTokenStore extends TokenStore
    {
        protected static const INITIAL_N_TOKENS:int = 6;
        
        protected var _board:ManantialesBoard;
        
        public function get board ():ManantialesBoard {
            return _board;
        }
        
        public function set board (board:ManantialesBoard):void {
            _board = board;
        }

        public function reset():void {
            while (_nTokens > 0) {
                removeToken();
            }
            
            for (var i:int = 0; i < INITIAL_N_TOKENS; i++) {
                addToken();
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