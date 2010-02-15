package mx.ecosur.multigame.enum
{

	[RemoteClass (alias="mx.ecosur.multigame.enum.MoveStatus")]
	public class MoveStatus
	{
        public static const INVALID:String = "INVALID";
        public static const VERIFIED:String = "VERIFIED";
        public static const UNVERIFIED:String = "UNVERIFIED";
        public static const MOVED:String = "MOVED";
        public static const EVALUATED:String = "EVALUATED";
    }
}