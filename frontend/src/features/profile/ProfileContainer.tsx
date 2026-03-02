import { useAuth0 } from "@auth0/auth0-react";
import { ProfileContent } from "./ProfileContent";
import { useProfileData } from "./useProfileData";

export function ProfileContainer() {
    const { isAuthenticated } = useAuth0();
    const data = useProfileData();

    return <ProfileContent isAuthenticated={isAuthenticated} data={data} />;
}
