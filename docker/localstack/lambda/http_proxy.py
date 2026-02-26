import json
import urllib.request
import urllib.error

ORDER_SERVICE = "http://host.docker.internal:8081"
PAYMENT_SERVICE = "http://host.docker.internal:8083"


def handler(event, context):
    action = event["action"]
    order_id = event.get("orderId", "")
    trace_id = event.get("traceId", "")

    if action == "CREATE_ORDER":
        method = "POST"
        url = f"{ORDER_SERVICE}/api/orders"
        body = {
            "orderId": order_id,
            "productId": event["productId"],
            "quantity": event["quantity"],
            "amount": event["amount"],
        }
    elif action == "EXECUTE_PAYMENT":
        method = "POST"
        url = f"{PAYMENT_SERVICE}/api/payments/authorize"
        body = {"orderId": order_id, "amount": event["amount"]}
    elif action == "CAPTURE_PAYMENT":
        method = "PUT"
        url = f"{PAYMENT_SERVICE}/api/payments/{order_id}/capture"
        body = None
    elif action == "COMPLETE_ORDER":
        method = "PUT"
        url = f"{ORDER_SERVICE}/api/orders/{order_id}/complete"
        body = None
    elif action == "COMPENSATE_REFUND":
        method = "PUT"
        url = f"{PAYMENT_SERVICE}/api/payments/{order_id}/refund"
        body = None
    elif action == "COMPENSATE_CANCEL":
        method = "PUT"
        url = f"{ORDER_SERVICE}/api/orders/{order_id}/cancel"
        body = None
    else:
        raise ValueError(f"Unknown action: {action}")

    headers = {"Content-Type": "application/json"}
    if trace_id:
        headers["X-Trace-Id"] = trace_id
    headers["Idempotency-Key"] = f"{action.lower().replace('_', '-')}-{order_id}"

    data = json.dumps(body).encode("utf-8") if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)

    try:
        with urllib.request.urlopen(req) as resp:
            resp_body = resp.read().decode("utf-8")
            return json.loads(resp_body) if resp_body else {}
    except urllib.error.HTTPError as e:
        error_body = e.read().decode("utf-8")
        raise Exception(f"HTTP {e.code}: {error_body}")
