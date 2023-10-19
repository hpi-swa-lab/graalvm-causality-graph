public final class HeapAssignmentTracingHooks
{
    public static native void onClinitStart();

    public static void onArrayWrite(Object[] arr, int index, Object val)
    {
        arr[index] = val;
    }
}