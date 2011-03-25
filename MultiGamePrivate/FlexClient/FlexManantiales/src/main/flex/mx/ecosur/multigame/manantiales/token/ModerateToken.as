package mx.ecosur.multigame.manantiales.token
{
	import mx.ecosur.multigame.enum.Color;
	   import mx.ecosur.multigame.enum.manantiales.TokenType;
	
    [ResourceBundle("Manantiales")]
	public class ModerateToken extends ManantialesToken
	{
		public function ModerateToken()
		{
			super();
			_tooltip = resourceManager.getString("Manantiales", "manantiales.token.moderate");
			_label = "M";
			_type = TokenType.MODERATE;
		}
	}
}