import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useLocation, useNavigate, useParams } from "react-router";
import { useAddCartItem } from "../features/cart/hooks";
import { useAuthStore } from "../stores/authStore";
import { getErrorMessage } from "../api/client";
import { getProduct } from "../features/product/api";
import { formatPrice } from "../lib/format";

export default function ProductDetailPage() {
  const { productId } = useParams();
  const id = Number(productId);
  const [quantity, setQuantity] = useState(1);
  const navigate = useNavigate();
  const location = useLocation();
  const accessToken = useAuthStore((state) => state.accessToken);
  const addToCart = useAddCartItem();

  const {
    data: product,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["product", id],
    queryFn: () => getProduct(id),
    enabled: Number.isInteger(id),
  });

  if (isLoading)
    return <p className="py-20 text-center text-gray-500">불러오는 중...</p>;

  if (isError || !product) {
    return (
      <div className="py-20 text-center">
        <p className="mb-4 text-gray-600">{getErrorMessage(error)}</p>
        <Link to="/products" className="underline">
          상품 목록으로
        </Link>
      </div>
    );
  }

  const soldOut = product.status === "SOLD_OUT" || product.stockQuantity === 0;
  const maxQuantity = Math.min(product.stockQuantity, 99);

  const handleAddToCart = () => {
    if (!accessToken) {
      navigate("/login", { state: { from: location.pathname } });
      return;
    }
    addToCart.mutate({ productId: product.id, quantity });
  };

  const changeQuantity = (next: number) => {
    setQuantity(next);
    addToCart.reset(); // 수량을 바꾸면 이전 "담았습니다" 문구 지우기
  };

  return (
    <div className="py-10">
      <div className="grid gap-10 md:grid-cols-2">
        <div className="aspect-square overflow-hidden rounded-xl bg-gray-100">
          {product.thumbnailUrl && (
            <img
              src={product.thumbnailUrl}
              alt={product.name}
              className="h-full w-full object-cover"
            />
          )}
        </div>

        <div>
          <Link
            to={`/products?categoryId=${product.categoryId}`}
            className="text-sm text-gray-500 hover:underline"
          >
            {product.categoryName}
          </Link>
          <h1 className="mt-2 text-2xl font-bold">{product.name}</h1>
          <p className="mt-4 text-2xl font-semibold">
            {formatPrice(product.price)}
          </p>
          <p
            className={`mt-2 text-sm ${soldOut ? "text-red-600" : "text-gray-500"}`}
          >
            {soldOut ? "품절된 상품입니다." : `재고 ${product.stockQuantity}개`}
          </p>

          {!soldOut && (
            <div className="mt-8 flex items-center gap-4">
              <span className="text-sm text-gray-700">수량</span>
              <div className="flex items-center rounded-md border border-gray-300">
                <button
                  className="px-3 py-1.5 disabled:opacity-30"
                  disabled={quantity <= 1}
                  onClick={() => changeQuantity(quantity - 1)}
                  aria-label="수량 줄이기"
                >
                  −
                </button>
                <span className="w-10 text-center text-sm">{quantity}</span>
                <button
                  className="px-3 py-1.5 disabled:opacity-30"
                  disabled={quantity >= maxQuantity}
                  onClick={() => changeQuantity(quantity + 1)}
                  aria-label="수량 늘리기"
                >
                  +
                </button>
              </div>
            </div>
          )}

          {!soldOut && (
            <p className="mt-6 text-sm text-gray-500">
              총 금액{" "}
              <span className="ml-2 text-lg font-bold text-gray-900">
                {formatPrice(product.price * quantity)}
              </span>
            </p>
          )}

          <button
            onClick={handleAddToCart}
            disabled={soldOut || addToCart.isPending}
            className="mt-6 w-full rounded-lg bg-gray-900 py-3 font-medium text-white disabled:opacity-40"
          >
            {soldOut
              ? "품절"
              : addToCart.isPending
                ? "담는 중..."
                : "장바구니 담기"}
          </button>

          {addToCart.isSuccess && (
            <p className="mt-3 text-sm text-green-700">
              장바구니에 담았습니다.{" "}
              <Link to="/cart" className="font-medium underline">
                장바구니 보기
              </Link>
            </p>
          )}
          {addToCart.isError && (
            <p className="mt-3 text-sm text-red-600">
              {getErrorMessage(addToCart.error)}
            </p>
          )}
        </div>
      </div>

      <section className="mt-16 border-t border-gray-200 pt-8">
        <h2 className="mb-4 text-lg font-bold">상품 설명</h2>
        <p className="whitespace-pre-line text-gray-700">
          {product.description}
        </p>
      </section>
    </div>
  );
}
