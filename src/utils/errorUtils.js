export function normalizeApiError(error) {
  const status = error?.response?.status;
  const payload = error?.response?.data;

  const messageFromPayload =
    payload?.message ||
    payload?.error ||
    payload?.details ||
    (typeof payload === 'string' ? payload : '');

  const message =
    messageFromPayload ||
    error?.message ||
    'Unexpected server error';

  return {
    status,
    message,
    isNetworkError: !error?.response,
    isTimeout: error?.code === 'ECONNABORTED',
    isRetryable: !status || status >= 500 || status === 408 || status === 429,
    original: error,
  };
}

export function getErrorMessage(error, fallback = 'Operation failed') {
  if (error?.message) return error.message;
  if (error?.response?.data?.message) return error.response.data.message;
  return fallback;
}
