import { Outlet } from "react-router-dom";
import type { RoleId } from "../../types/auth.types";

interface PrivateRouteProps {
  allowedRoles?: RoleId[];
}

export function PrivateRoute(_props: PrivateRouteProps) {
  // Auth checks disabled temporarily for full navigability in dev preview.
  return <Outlet />;
}