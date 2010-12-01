package mx.ecosur.multigame.model.interfaces
{
    import mx.ecosur.multigame.entity.Cell;

    [RemoteClass (alias="mx.ecosur.multigame.model.interfaces.MoveImpl")]
    public interface MoveImplementation extends Implementation
    {
        function get id ():int;

        function set id (id:int):void;

        function get status ():String;

        function set status(status:String):void;

        function get currentCell():Cell;

        function set currentCell(cell:Cell):void;

        function get destinationCell():Cell;

        function set destinationCell(cell:Cell):void;

    }
}
