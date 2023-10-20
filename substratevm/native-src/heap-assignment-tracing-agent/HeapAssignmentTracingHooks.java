public final class HeapAssignmentTracingHooks
{
    private static volatile byte onArrayWriteDummyField;

    public static native void onClinitStart();

    public static void onArrayWrite(Object[] arr, int index, Object val)
    {
        arr[index] = val;
        // Picking up the Field modification event turns out to be much faster than inserting a breakpoint
        onArrayWriteDummyField = 0;
    }
}