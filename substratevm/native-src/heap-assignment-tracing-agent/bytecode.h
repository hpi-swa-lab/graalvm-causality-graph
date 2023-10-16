#ifndef HEAP_ASSIGNMENT_TRACING_AGENT_BYTECODE_H
#define HEAP_ASSIGNMENT_TRACING_AGENT_BYTECODE_H

class ClassRewritingException : public std::exception
{
    const char* msg;

public:
    ClassRewritingException(const char* msg) noexcept : msg(msg)
    {}

    const char* what() const noexcept override
    {
        return msg;
    }
};

size_t add_clinit_hook(const unsigned char* src_start, jint src_len, unsigned char* dst_start, jint dst_len);

#endif //HEAP_ASSIGNMENT_TRACING_AGENT_BYTECODE_H
