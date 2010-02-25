package mx.ecosur.multigame.manantiales.token
{
    import flash.display.Shape;

    import mx.ecosur.multigame.component.TokenStore;

    public class ManantialesTokenStore extends TokenStore
    {
        protected static const INITIAL_N_TOKENS:int = 6;

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