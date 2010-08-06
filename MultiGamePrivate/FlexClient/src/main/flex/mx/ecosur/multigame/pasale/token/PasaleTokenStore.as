package mx.ecosur.multigame.pasale.token
{
	import flash.display.Shape;
	import flash.events.MouseEvent;
    import mx.controls.Label;
	import mx.ecosur.multigame.component.Token;
	import mx.ecosur.multigame.component.TokenStore;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.enum.Color;
	import mx.ecosur.multigame.manantiales.enum.TokenType;
	import mx.events.DragEvent;

	public class PasaleTokenStore extends TokenStore
	{
		protected static const INITIAL_N_TOKENS:int = 12;

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
	        var token:Token;
	        _nTokens = 0;
	        for (var i:int = 0; i < INITIAL_N_TOKENS; i++){
	            addToken();
	        }
        }
    }
}