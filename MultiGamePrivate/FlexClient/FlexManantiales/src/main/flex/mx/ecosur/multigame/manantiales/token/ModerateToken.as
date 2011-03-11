package mx.ecosur.multigame.manantiales.token
{
	import mx.ecosur.multigame.enum.Color;
	   import mx.ecosur.multigame.manantiales.enum.TokenType;
	
	public class ModerateToken extends ManantialesToken
	{
		public function ModerateToken()
		{
			super();
			_tooltip = resourceManager.getString("StringsBundle", "manantiales.token.moderate");
			_label = "M";
			_type = TokenType.MODERATE;
		}
	}
}